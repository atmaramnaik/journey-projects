package com.atmaram.tp.template.text;

import com.atmaram.tp.template.Variable;
import com.atmaram.tp.common.ExpressionProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class TextLoopTemplate implements TextTemplate{
    String variableName;
    TextTemplate innerTemplate;

    public TextLoopTemplate(String variableName, TextTemplate innerTemplate) {
        this.variableName = variableName;
        this.innerTemplate = innerTemplate;
    }

    @Override
    public TextTemplate fill(HashMap<String, Object> data,boolean lazy) {
        Object loopVariable= ExpressionProcessor.process(variableName,data);
        if(loopVariable instanceof List){
            ArrayTextTemplate arrayTextTemplate=new ArrayTextTemplate();
            for (Object listElement:
                    (List)loopVariable) {
                if(listElement instanceof HashMap){
                    arrayTextTemplate.add((TextTemplate) innerTemplate.fill((HashMap<String, Object>) listElement,lazy));
                } else {
                    HashMap<String,Object> listData=new HashMap<>();
                    listData.put("_this",listElement);
                    arrayTextTemplate.add((TextTemplate)innerTemplate.fill(listData,lazy));
                }
            }
            return arrayTextTemplate;
        } else {
            return new TextLoopTemplate(variableName,(TextTemplate)innerTemplate.fill(data,lazy));
        }
    }

    @Override
    public String toStringTemplate() {
        String ret="";
        ret+="{{#"+variableName+"}}"+innerTemplate.toStringTemplate()+"{{/"+variableName+"}}";
        return ret;
    }

    @Override
    public List<Variable> getVariables() {
        List<Variable> returnValue=new ArrayList<>();
        Variable variable=new Variable();
        variable.setName(variableName);
        variable.setType("List");
        List<Variable> inner_variables=innerTemplate.getVariables();
        List<Variable> inner_variables_excluding_this=new ArrayList<>();
        boolean found_this_variable=false;
        for (Variable inner_variable:inner_variables
                ) {
            if(inner_variable.getName().equals("_this")){
                found_this_variable=true;
            } else {
                inner_variables_excluding_this.add(inner_variable);
            }
        }
        if(found_this_variable){
            returnValue.add(variable);
            returnValue.addAll(inner_variables_excluding_this);
        } else {
            variable.setInner_variables(inner_variables_excluding_this);
            returnValue.add(variable);
        }
        return returnValue;
    }
}
