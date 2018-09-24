package com.google.refine.exporters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.LoggerFactory;


import com.google.refine.ProjectManager;
import com.google.refine.browsing.Engine;
import com.google.refine.exporters.HtmlTableExporter;
import com.google.refine.exporters.WriterExporter;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ModelException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.model.metadata.ProjectMetadata;
import com.google.refine.tests.ProjectManagerStub;
import com.google.refine.tests.RefineTest;
import com.google.refine.tests.exporters.HtmlExporterTests;

//import static org.junit.Assert.*;

import org.junit.*;


public class HaydenHtmlTableExporterTest extends HtmlExporterTests {

    private static final String TEST_PROJECT_NAME = "html table exporter test project";

    @Override
    //@BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    //dependencies
    StringWriter writer;
    ProjectMetadata projectMetadata;
    Project project;
    Engine engine;
    Properties options;

    //System Under Test
    WriterExporter SUT;

    @Before
    public void SetUp(){
        SUT = new HtmlTableExporter();
        writer = new StringWriter();
        ProjectManager.singleton = new ProjectManagerStub();
        projectMetadata = new ProjectMetadata();
        project = new Project();
        projectMetadata.setName(TEST_PROJECT_NAME);
        ProjectManager.singleton.registerProject(project, projectMetadata);
        engine = new Engine(project);
        options = mock(Properties.class);
    }

    @After
    public void TearDown(){
        SUT = null;
        writer = null;
        ProjectManager.singleton.deleteProject(project.id);
        project = null;
        projectMetadata = null;
        engine = null;
        options = null;
    }

    @Test
    public void exportSimpleHtmlTable(){
        CreateGrid(2, 2);
        try {
            SUT.export(project, options, engine, writer);
        } catch (IOException e) {
            Assert.fail();
        }

        Assert.assertEquals(writer.toString(), "<html>\n" +
                "<head>\n" + "<title>" + TEST_PROJECT_NAME + "</title>\n" + 
                "<meta charset=\"utf-8\" />\n" + 
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><th>column0</th><th>column1</th></tr>\n" +
                "<tr><td>row0cell0</td><td>row0cell1</td></tr>\n" +
                "<tr><td>row1cell0</td><td>row1cell1</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
        "</html>\n");
        
    }

    // This test used to fail because the HTML table exporter 
    // didn't honor the column header option.
    @Test
    public void exportSimpleHtmlTableNoHeader(){
        CreateGrid(2, 2);
        when(options.getProperty("printColumnHeader")).thenReturn("false");
        try {
            SUT.export(project, options, engine, writer);
        } catch (IOException e) {
            Assert.fail();
        }
        System.out.println(writer.toString());
        Assert.assertEquals(writer.toString(), "<html>\n" +
                "<head>\n" + "<title>" + TEST_PROJECT_NAME + "</title>\n" + 
                "<meta charset=\"utf-8\" />\n" + 
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><td>row0cell0</td><td>row0cell1</td></tr>\n" +
                "<tr><td>row1cell0</td><td>row1cell1</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n");
        verify(options,times(2)).getProperty("printColumnHeader");
    }

    @Test
    public void exportHtmlTableWithEmptyCells(){
        CreateGrid(3,3);

        project.rows.get(1).cells.set(1, null);
        project.rows.get(2).cells.set(0, null);
        try {
            SUT.export(project, options, engine, writer);
        } catch (IOException e) {
            Assert.fail();
        }

        Assert.assertEquals(writer.toString(), "<html>\n" +
                "<head>\n" + "<title>" + TEST_PROJECT_NAME + "</title>\n" + 
                "<meta charset=\"utf-8\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><th>column0</th><th>column1</th><th>column2</th></tr>\n" +
                "<tr><td>row0cell0</td><td>row0cell1</td><td>row0cell2</td></tr>\n" +
                "<tr><td>row1cell0</td><td></td><td>row1cell2</td></tr>\n" +
                "<tr><td></td><td>row2cell1</td><td>row2cell2</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n");
        }
    
    @Test
    public void exportHtmlTableWithURLs(){
        CreateGrid(3,3);

        project.rows.get(1).cells.set(1, new Cell("ftp://ftp.ripe.net/ripe/", null));
        project.rows.get(2).cells.set(0, new Cell("https://gnu.org/", null));
        try {
            SUT.export(project, options, engine, writer);
        } catch (IOException e) {
            Assert.fail();
        }

        Assert.assertEquals(writer.toString(), "<html>\n" +
                "<head>\n" + "<title>" + TEST_PROJECT_NAME + "</title>\n" + 
                "<meta charset=\"utf-8\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><th>column0</th><th>column1</th><th>column2</th></tr>\n" +
                "<tr><td>row0cell0</td><td>row0cell1</td><td>row0cell2</td></tr>\n" +
                "<tr><td>row1cell0</td><td><a href=\"ftp://ftp.ripe.net/ripe/\">ftp://ftp.ripe.net/ripe/</a></td><td>row1cell2</td></tr>\n" +
                "<tr><td><a href=\"https://gnu.org/\">https://gnu.org/</a></td><td>row2cell1</td><td>row2cell2</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n");
    }

    //helper methods

    protected void CreateColumns(int noOfColumns){
        for(int i = 0; i < noOfColumns; i++){
            try {
                project.columnModel.addColumn(i,
                        new Column(i, "column" + i),
                        true);
            } catch (ModelException e1) {
                System.out.println("Hello?");
                Assert.fail("Could not create column");
            }
        }
    }

    protected void CreateGrid(int noOfRows, int noOfColumns){
        CreateColumns(noOfColumns);

        for(int i = 0; i < noOfRows; i++){
            Row row = new Row(noOfColumns);
            for(int j = 0; j < noOfColumns; j++){
                row.cells.add(new Cell("row" + i + "cell" + j, null));
            }
            project.rows.add(row);
        }
    }

}
