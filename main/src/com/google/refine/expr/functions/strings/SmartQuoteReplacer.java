package com.google.refine.expr.functions.strings;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.ControlFunctionRegistry;
import com.google.refine.grel.Function;


public class SmartQuoteReplacer implements Function
{

    @Override
    public void write(JSONWriter writer, Properties options)
        throws JSONException {
    
        writer.object();
        writer.key("description"); writer.value("Returns s that has all smart quotes replace with \"");
        writer.key("params"); writer.value("string s");
        writer.key("returns"); writer.value("string");
        writer.endObject();
    }

    @Override
    public Object call(Properties bindings, Object[] args) {
        if (args.length == 1 && args[0] != null) {
            Object o = args[0];
            if(o instanceof String)
            {
                o = o.toString().replaceAll("“", "\"");
                o = o.toString().replaceAll("”", "\"");
            }
            return o;
        }
        return new EvalError(ControlFunctionRegistry.getFunctionName(this) + " expects a string");
    }
    
}
