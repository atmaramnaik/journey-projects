package com.atmaram.tp.template.text;

import com.atmaram.tp.common.exceptions.TemplateParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TextTemplateTest {
    @Test
    public void should_fill_expressions_having_uuid() throws TemplateParseException {
        String obj=TextTemplate.parse("${_uuid}").fill(null).toStringTemplate();
        assertThat(obj).isInstanceOf(String.class);
    }

    @Test
    public void should_fill_expressions_having_eval() throws TemplateParseException {
        String obj=TextTemplate.parse("${_eval('####')}").fill(null).toStringTemplate();
        assertThat(obj).isInstanceOf(String.class);
        assertThat(obj.length()).isEqualTo(4);
    }
    @Test
    public void should_fill_expressions_having_timestamp() throws TemplateParseException {
        String obj=TextTemplate.parse("${_timestamp}").fill(null).toStringTemplate();
        assertThat(obj).isInstanceOf(String.class);
    }
    @Test
    public void should_fill_expressions_having_ref() throws TemplateParseException {
        String obj=TextTemplate.parse("${_timestamp>ref1}${ref1}").fill(new HashMap<>()).toStringTemplate();
        assertThat(obj).isInstanceOf(String.class);
        for (int i=0;i<(obj.length()/2);i++){
            assertThat(obj.charAt(i)==obj.charAt(obj.length()/2+i));
        }
    }
    @Test
    public void should_parse_text_template_without_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello");
        assertThat(textTemplate).isInstanceOf(FilledTextTemplate.class);
        assertThat(((FilledTextTemplate)textTemplate).value).isEqualTo("Hello");

    }
    @Test
    public void should_parse_text_template_with_single_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello ${to}");
        assertThat(textTemplate).isInstanceOf(ArrayTextTemplate.class);
        ArrayTextTemplate arrayTextTemplate=(ArrayTextTemplate)textTemplate;
        assertThat(arrayTextTemplate.blocks.size()).isEqualTo(2);
        assertThat(arrayTextTemplate.blocks.get(0).toStringTemplate()).isEqualTo("Hello ");
        assertThat(arrayTextTemplate.blocks.get(1)).isInstanceOf(TextVariableTemplate.class);
        TextVariableTemplate textVariableTemplate=(TextVariableTemplate) arrayTextTemplate.blocks.get(1);
        assertThat(textVariableTemplate.variableName).isEqualTo("to");
    }
    @Test
    public void should_parse_text_template_with_multiple_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello ${to} from ${from}");
        assertThat(textTemplate).isInstanceOf(ArrayTextTemplate.class);
        ArrayTextTemplate arrayTextTemplate=(ArrayTextTemplate)textTemplate;
        assertThat(arrayTextTemplate.blocks.size()).isEqualTo(4);
        assertThat(arrayTextTemplate.blocks.get(0).toStringTemplate()).isEqualTo("Hello ");
        assertThat(arrayTextTemplate.blocks.get(1)).isInstanceOf(TextVariableTemplate.class);
        TextVariableTemplate textVariable1=(TextVariableTemplate)arrayTextTemplate.blocks.get(1);
        assertThat(textVariable1.variableName).isEqualTo("to");
        assertThat(arrayTextTemplate.blocks.get(2).toStringTemplate()).isEqualTo(" from ");

        assertThat(arrayTextTemplate.blocks.get(3)).isInstanceOf(TextVariableTemplate.class);
        TextVariableTemplate textVariable2=(TextVariableTemplate)arrayTextTemplate.blocks.get(3);
        assertThat(textVariable2.variableName).isEqualTo("from");
    }
    @Test
    public void should_parse_text_template_with_loop() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello {{#names}}name{{/names}}");
        assertThat(textTemplate).isInstanceOf(ArrayTextTemplate.class);
        ArrayTextTemplate arrayTextTemplate=(ArrayTextTemplate)textTemplate;
        assertThat(arrayTextTemplate.blocks.size()).isEqualTo(2);
        assertThat(arrayTextTemplate.blocks.get(0).toStringTemplate()).isEqualTo("Hello ");
        assertThat(arrayTextTemplate.blocks.get(1)).isInstanceOf(TextLoopTemplate.class);
        TextLoopTemplate textLoop=(TextLoopTemplate)arrayTextTemplate.blocks.get(1);
        assertThat(textLoop.variableName).isEqualTo("names");
        assertThat(textLoop.innerTemplate).isInstanceOf(FilledTextTemplate.class);
        assertThat(((FilledTextTemplate)textLoop.innerTemplate).value).isEqualTo("name");

    }
    @Test
    public void should_parse_text_template_with_nested_loops() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello {{#places}}{{#names}}names{{/names}}{{/places}}");
        assertThat(textTemplate).isInstanceOf(ArrayTextTemplate.class);
        ArrayTextTemplate arrayTextTemplate=(ArrayTextTemplate)textTemplate;
        assertThat(arrayTextTemplate.blocks.size()).isEqualTo(2);
        assertThat(arrayTextTemplate.blocks.get(0).toStringTemplate()).isEqualTo("Hello ");
        assertThat(arrayTextTemplate.blocks.get(1)).isInstanceOf(TextLoopTemplate.class);
        TextLoopTemplate loopTemplate=(TextLoopTemplate)arrayTextTemplate.blocks.get(1);
        assertThat(loopTemplate.variableName).isEqualTo("places");
        assertThat(loopTemplate.innerTemplate).isInstanceOf(TextLoopTemplate.class);
        TextLoopTemplate innerLoopTemplate=(TextLoopTemplate)loopTemplate.innerTemplate;
        assertThat(innerLoopTemplate.variableName).isEqualTo("names");
        assertThat(innerLoopTemplate.innerTemplate).isInstanceOf(FilledTextTemplate.class);
        assertThat(innerLoopTemplate.innerTemplate.toStringTemplate()).isEqualTo("names");

    }

    //Fill test
    @Test
    public void should_return_same_template_when_no_variables() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello");
        String result=textTemplate.fill(null).toStringTemplate();
        assertThat(result).isEqualTo("Hello");
    }
    @Test
    public void should_fill_single_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello ${to}");
        HashMap<String,Object> data=new HashMap<>();
        data.put("to","World");
        String result=textTemplate.fill(data).toStringTemplate();
        assertThat(result).isEqualTo("Hello World");
    }
    @Test
    public void should_fill_loop_with_blank_text() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello{{#names}}{{/names}}");
        HashMap<String,Object> data=new HashMap<>();
        List<String> names=new ArrayList<>();
        names.add("Atmaram");
        names.add("Roopa");
        data.put("names",names);
        String result=textTemplate.fill(data).toStringTemplate();
        assertThat(result).isEqualTo("Hello");
    }
    @Test
    public void should_fill_loop_with_static_text() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello {{#names}}name {{/names}}");
        HashMap<String,Object> data=new HashMap<>();
        List<String> names=new ArrayList<>();
        names.add("Atmaram");
        names.add("Roopa");
        data.put("names",names);
        String result=textTemplate.fill(data).toStringTemplate();
        assertThat(result).isEqualTo("Hello name name ");
    }
    @Test
    public void should_fill_loop_with_variable_text() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello {{#names}}${_this} {{/names}}");
        HashMap<String,Object> data=new HashMap<>();
        List<String> names=new ArrayList<>();
        names.add("Atmaram");
        names.add("Roopa");
        data.put("names",names);
        String result=textTemplate.fill(data).toStringTemplate();
        assertThat(result).isEqualTo("Hello Atmaram Roopa ");
    }
    @Test
    public void should_not_fill_loop_when_no_loop_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("Hello {{#names}}${name}{{/names}}");
        HashMap<String,Object> data=new HashMap<>();
        String result=textTemplate.fill(data).toStringTemplate();
        assertThat(result).isEqualTo("Hello {{#names}}${name}{{/names}}");
    }
    @Rule
    public ExpectedException expectedException=ExpectedException.none();

    @Test
    public void should_throw_template_parse_exception_for_xml_template() throws TemplateParseException {
        expectedException.expect(TemplateParseException.class);
        expectedException.expectMessage("No closing Tag found for tag: {{#test}}");
        TextTemplate.parse("{{#test}}Hello");
    }
    @Test
    public void should_parse_nested_loops_with_same_variable() throws TemplateParseException {
        TextTemplate textTemplate=TextTemplate.parse("{{#test}}{{#test}}Hello{{/test}}{{/test}}");
        assertThat(textTemplate).isInstanceOf(TextLoopTemplate.class);
        assertThat(((TextLoopTemplate)textTemplate).innerTemplate).isInstanceOf(TextLoopTemplate.class);
    }
}
