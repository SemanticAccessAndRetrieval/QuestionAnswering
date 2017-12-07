/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sailInfoBase;

import gr.forth.ics.isl.sailInfoBase.SailInfoBase;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;

/**
 * This class is responsible for testing the stability of our sail knowledge
 * base.
 *
 * @author Sgo
 */
public class SailInfoBaseTest implements java.io.Serializable {

    SailInfoBase instance;

    public SailInfoBaseTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws RepositoryException, IOException {
        instance = new SailInfoBase();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadDataToRepo method, of class SailInfoBase.
     * @throws java.lang.Exception
     */
    //@org.junit.Test
    public void testLoadDataToRepo() throws Exception {
        System.out.println("loadDataToRepo");
        assert (instance.getRepo().isInitialized());
    }

    /**
     * Test of queryRepo method, of class SailInfoBase.
     * @throws java.lang.Exception
     */
    //@org.junit.Test
    public void testQueryRepo() throws Exception {
        System.out.println("queryRepo");
        
        String query = "select  ?p ?x ?z "
                + "where {"
                + "?p a csdT:Course  ."
                + "?p csdT:academicYear  ?x ."
                + "optional{"
                + "?p csdT:courseRequired  ?z ."
                + "}"
                + "}order by asc(?x)";

        HashSet<ArrayList<String>> result = instance.queryRepo(query);
        System.out.println("queryRepo22222");
        //saveResult(result, "testQueryRepo");
        HashSet<ArrayList<String>> expResult = (HashSet<ArrayList<String>>) getExpResult("testQueryRepo");
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of prepareQuery method, of class SailInfoBase.
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    //@org.junit.Test
    public void testPrepareQuery() throws IOException, FileNotFoundException, ClassNotFoundException {
        System.out.println("prepareQuery");
        
        String query = "select  ?p ?x ?z "
                + "where {"
                + "?p a csdT:Course  ."
                + "?p csdT:academicYear  ?x ."
                + "optional{"
                + "?p csdT:courseRequired  ?z ."
                + "}"
                + "}order by asc(?x)";
        
        String result = instance.prepareQuery(query);
        //saveResult(result, "testPrepareQuery");
        String expResult = (String) getExpResult("testPrepareQuery");
        
        assertEquals(expResult, result);
    }

    public void saveResult(Object result, String fileName) throws FileNotFoundException, IOException {
        // Write to disk with FileOutputStream
        FileOutputStream f_out = new FileOutputStream("src/test/resources/" + fileName + ".data");

        // Write object with ObjectOutputStream
        ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

        // Write object out to disk
        obj_out.writeObject(result);
    }

    public Object getExpResult(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {

        // Read from disk using FileInputStream
        FileInputStream f_in = new FileInputStream("src/test/resources/" + fileName + ".data");

        // Read object using ObjectInputStream
        ObjectInputStream obj_in
                = new ObjectInputStream(f_in);

        // Read an object
        Object expResult = obj_in.readObject();

        return expResult;
    }
}
