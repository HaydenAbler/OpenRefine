
package com.google.refine.tests.grel.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.expr.Evaluable;
import com.google.refine.grel.ast.VariableExpr;
import com.google.refine.grel.controls.ForEach;
import com.google.refine.grel.controls.ForEachIndex;
import com.google.refine.tests.util.TestUtils;

public class ForEachTests {

    ForEach sut;

    @BeforeTest
    public void setup() {
        sut = new ForEach();
    }

    @Test
    public void serializeForEach() {
        String json = "{\"description\":\"Evaluates expression a to an array. Then for each array element, binds its value to variable name v, evaluates expression e, and pushes the result onto the result array.\",\"params\":\"expression a, variable v, expression e\",\"returns\":\"array\"}";
        TestUtils.isSerializedTo(new ForEach(), json);
    }

    @Test
    public void checkArguementsWithInvalidNumberOfArguements() {
        Evaluable[] args = new Evaluable[5];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsWithInvalidSecondArguement() {
        Evaluable[] args = new Evaluable[3];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsReturnsNullWithValidArguments() {
        Evaluable[] args = new Evaluable[3];
        args[1] = new VariableExpr("asf");
        args[2] = new VariableExpr("asf");
        assertEquals(sut.checkArguments(args), null);
    }
}
