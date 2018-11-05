package com.google.refine.tests.grel.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.expr.Evaluable;
import com.google.refine.grel.ast.VariableExpr;
import com.google.refine.grel.controls.ForEachIndex;
import com.google.refine.tests.util.TestUtils;

public class ForEachIndexTests {
    
    ForEachIndex sut;
    
    @BeforeTest
    public void setup() {
        sut = new ForEachIndex();
    }
    
    @Test
    public void serializeForEachIndex() {
        String json = "{\"description\":\"Evaluates expression a to an array. Then for each array element, binds its index to variable i and its value to variable name v, evaluates expression e, and pushes the result onto the result array.\",\"params\":\"expression a, variable i, variable v, expression e\",\"returns\":\"array\"}";
        TestUtils.isSerializedTo(new ForEachIndex(), json);
    }
    
    @Test 
    public void checkArguementsWithInvalidNumberOfArguements()       {
        Evaluable[] args = new Evaluable[3];
        assertNotEquals(sut.checkArguments(args), null);
    }
    
    @Test 
    public void checkArguementsWithInvalidSecondArguement()       {
        Evaluable[] args = new Evaluable[4];
        assertNotEquals(sut.checkArguments(args), null);
    }
    
    @Test 
    public void checkArguementsWithInvalidThirdArguement()       {
        Evaluable[] args = new Evaluable[4];
        args[1] = new VariableExpr("asf");
        assertNotEquals(sut.checkArguments(args), null);
    }
    
    @Test 
    public void checkArguementsReturnsNullWithValidArguments()       {
        Evaluable[] args = new Evaluable[4];
        args[1] = new VariableExpr("asf");
        args[2] = new VariableExpr("asf");
        assertEquals(sut.checkArguments(args), null);
    }
}

