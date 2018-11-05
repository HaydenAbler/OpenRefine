package com.google.refine.tests.grel.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.expr.Evaluable;
import com.google.refine.grel.ast.VariableExpr;
import com.google.refine.grel.controls.ForNonBlank;
import com.google.refine.grel.controls.ForRange;
import com.google.refine.tests.util.TestUtils;

public class ForRangeTests {
    ForRange sut;

    @BeforeTest
    public void setup() {
        sut = new ForRange();
    }
    
    @Test
    public void serializeForRange() {
        String json = "{\"description\":\"Iterates over the variable v starting at \\\"from\\\", incrementing by \\\"step\\\" each time while less than \\\"to\\\". At each iteration, evaluates expression e, and pushes the result onto the result array.\",\"params\":\"number from, number to, number step, variable v, expression e\",\"returns\":\"array\"}";
        TestUtils.isSerializedTo(new ForRange(), json);
    }
    
    @Test
    public void checkArguementsWithInvalidNumberOfArguements() {
        Evaluable[] args = new Evaluable[3];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsWithInvalidFourthArguement() {
        Evaluable[] args = new Evaluable[5];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsReturnsNullWithValidArguments() {
        Evaluable[] args = new Evaluable[5];
        args[1] = new VariableExpr("asf");
        args[3] = new VariableExpr("asf");
        assertEquals(sut.checkArguments(args), null);
    }
    
}

