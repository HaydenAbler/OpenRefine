
package com.google.refine.tests.grel.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.expr.Evaluable;
import com.google.refine.grel.ast.VariableExpr;
import com.google.refine.grel.controls.ForEach;
import com.google.refine.grel.controls.ForNonBlank;
import com.google.refine.tests.util.TestUtils;

public class ForNonBlankTests {

    ForNonBlank sut;

    @BeforeTest
    public void setup() {
        sut = new ForNonBlank();
    }

    @Test
    public void serializeForNonBlank() {
        String json = "{\"description\":\"Evaluates expression o. If it is non-blank, binds its value to variable name v, evaluates expression eNonBlank and returns the result. Otherwise (if o evaluates to blank), evaluates expression eBlank and returns that result instead.\",\"params\":\"expression o, variable v, expression eNonBlank, expression eBlank\",\"returns\":\"Depends on actual arguments\"}";
        TestUtils.isSerializedTo(new ForNonBlank(), json);
    }

    @Test
    public void checkArguementsWithInvalidNumberOfArguements() {
        Evaluable[] args = new Evaluable[3];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsWithInvalidSecondArguement() {
        Evaluable[] args = new Evaluable[4];
        assertNotEquals(sut.checkArguments(args), null);
    }

    @Test
    public void checkArguementsReturnsNullWithValidArguments() {
        Evaluable[] args = new Evaluable[4];
        args[1] = new VariableExpr("asf");
        args[2] = new VariableExpr("asf");
        assertEquals(sut.checkArguments(args), null);
    }

}
