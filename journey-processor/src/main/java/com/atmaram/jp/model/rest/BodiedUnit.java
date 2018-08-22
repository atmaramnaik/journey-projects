package com.atmaram.jp.model.rest;

import com.atmaram.jp.RestClient;
import com.atmaram.jp.ValueStore;
import com.atmaram.jp.VariableStore;
import com.atmaram.jp.exceptions.UnitConfigurationException;
import com.atmaram.jp.model.Unit;
import com.atmaram.tp.Variable;
import com.atmaram.tp.common.exceptions.TemplateParseException;
import com.atmaram.tp.json.JSONTemplate;
import lombok.Data;
import org.json.simple.JSONAware;

import java.util.List;

@Data
public abstract class BodiedUnit extends RestUnit {
    String requestTemplate;

    public BodiedUnit(RestClient restClient) {
        super(restClient);
    }

    public Unit fillObject(BodiedUnit bodiedUnit, ValueStore valueStore){
        fillObject((RestUnit) bodiedUnit,valueStore);
        String body = requestTemplate;
        try {
            JSONTemplate rTemplate=JSONTemplate.parse(body).fill(valueStore.getValues());
            if(rTemplate.toJSONCompatibleObject() instanceof JSONAware)
                bodiedUnit.requestTemplate = ((JSONAware) rTemplate.toJSONCompatibleObject()).toJSONString();
            else{
                bodiedUnit.requestTemplate=rTemplate.toJSONCompatibleObject().toString();
            }
        } catch (TemplateParseException e) {
            e.printStackTrace();
        }
        return bodiedUnit;
    }

    @Override
    public void eval(VariableStore variableStore) throws UnitConfigurationException {
        super.eval(variableStore);
        List<Variable> bodyVariables = null;
        try {
            bodyVariables = JSONTemplate.parse(requestTemplate).getVariables();
        } catch (TemplateParseException e) {
            throw new UnitConfigurationException("Invalid Template in request body: "+this.getName(),this.name,e);
        }
        variableStore.add(bodyVariables);
    }
}