/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plugtree.training.drools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.Person;
import org.drools.agent.KnowledgeAgent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author esteban
 */
public class KBaseModificationInsideRHSTest extends BaseKnowledgeAgentTest{

    private KnowledgeAgent kagent;
    private String rule1;
    private String rule2;
    
    public KBaseModificationInsideRHSTest() {
    }

    @Test
    public void testKbaseModificationWhileFiringAllRules() throws Exception{
        //Create a Rule that, when fired, will modify the kbase adding a new 
        //rule.
        rule1 = "";
        rule1 += "package org.drools\n";
        rule1 += "import org.drools.Person;\n";
        rule1 += "import org.plugtree.training.drools.KBaseModificationInsideRHSTest;\n";
        rule1 += "global java.util.List list;\n";
        rule1 += "global KBaseModificationInsideRHSTest testClass;\n";
        rule1 += "rule \"Rule 1\"\n";
        rule1 += "  when\n";
        rule1 += "      Person(name==\"John\")\n";
        rule1 += "  then\n";
        rule1 += "      list.add(\"Rule1\");\n";
        rule1 += "      testClass.modifyRuleFileAndForceScan();\n";
        rule1 += "end\n";
        
        //Write the rule into rules.drl file
        fileManager.write( "rules.drl",
                           rule1);        

        //Create a change-set pointing to rules.drl
        String xml = "";
        xml += "<change-set xmlns='http://drools.org/drools-5.0/change-set'";
        xml += "    xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'";
        xml += "    xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >";
        xml += "    <add> ";
        xml += "        <resource source='http://localhost:" + this.getPort() + "/rules.drl' type='DRL' />";
        xml += "    </add> ";
        xml += "</change-set>";
        File fxml = fileManager.write( "changeset.xml",
                                       xml );

        //Get an empty kbase and use a kagent to populate it
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kagent = this.createKAgent( kbase, false );

        //Apply the change-set. After this, the kbase should contain only 1 rule
        applyChangeSet(kagent, ResourceFactory.newUrlResource( fxml.toURI().toURL() ) );

        //Get a session from kbase
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        //Add global variables
        List<String> executionsList = new ArrayList<String>();
        ksession.setGlobal("testClass", this);
        ksession.setGlobal("list", executionsList);
        
        //Insert a Person with name "John". This will create an activation.
        ksession.insert(new Person("John", 21));

        //Fire all the rules. The execution of Rule 1 will add a new rule to
        //kbase (see modifyRuleFileAndForceScan()). Because the inserted
        //Person matches the LHS of the new rule, the rule will be activated
        ksession.fireAllRules();

        //So, we have 2 rules in the kbase
        assertEquals(2, kbase.getKnowledgePackage("org.drools").getRules().size());
        //And both rules were executed:
        assertEquals(2,executionsList.size());
        assertTrue(executionsList.contains("Rule1"));
        assertTrue(executionsList.contains("Rule2"));
        
        //Clear the global list
        executionsList.clear();
                
        //Dispose ksession and kagent
        ksession.dispose();
        kagent.dispose();
    }
    
    
    @Test
    public void testKBaseModificationAfterFiringAllRules() throws Exception{
        //Create a Rule
        rule1 = "";
        rule1 += "package org.drools\n";
        rule1 += "import org.drools.Person;\n";
        rule1 += "global java.util.List list;\n";
        rule1 += "rule \"Rule 1\"\n";
        rule1 += "  when\n";
        rule1 += "      $p : Person(name==\"John\")\n";
        rule1 += "  then\n";
        rule1 += "      list.add(\"Rule1\");\n";
        rule1 += "end\n";
        
        //Write the rule into rules.drl file
        fileManager.write( "rules.drl",
                           rule1);        

        //Create a change-set pointing to rules.drl
        String xml = "";
        xml += "<change-set xmlns='http://drools.org/drools-5.0/change-set'";
        xml += "    xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'";
        xml += "    xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >";
        xml += "    <add> ";
        xml += "        <resource source='http://localhost:" + this.getPort() + "/rules.drl' type='DRL' />";
        xml += "    </add> ";
        xml += "</change-set>";
        File fxml = fileManager.write( "changeset.xml",
                                       xml );

        //Get an empty kbase and use a kagent to populate it
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kagent = this.createKAgent( kbase, false );

        //Apply the change-set. After this, the kbase should contain only 1 rule
        applyChangeSet(kagent, ResourceFactory.newUrlResource( fxml.toURI().toURL() ) );

        //Get a session from kbase
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        //Add global variable
        List<String> executionsList = new ArrayList<String>();
        ksession.setGlobal("list", executionsList);
        
        //Insert a Person with name "John". This will create an activation.
        ksession.insert(new Person("John", 21));

        //Fire all the rules.
        ksession.fireAllRules();

        //So, we only have 1 element in our list: "Rule1".
        assertEquals(1,executionsList.size());
        assertTrue(executionsList.contains("Rule1"));
        //And we olny have 1 rule in the kbase
        assertEquals(1, kbase.getKnowledgePackage("org.drools").getRules().size());
        
        
        //Update the kbase
        this.modifyRuleFileAndForceScan();
        
        //Clear the global list
        executionsList.clear();
        
        //Activations?
        ksession.fireAllRules();
        //Yes! The old fact activated the new rule without having to do an
        //explicit update()
        assertFalse(executionsList.isEmpty());
        
        //Rule 2 got executed
        assertTrue(executionsList.contains("Rule2"));
        
        //Dispose ksession and kagent
        ksession.dispose();
        kagent.dispose();
    }
    
    public void modifyRuleFileAndForceScan() throws IOException{
        rule2 = "\n\n";
        rule2 += "rule \"Rule 2\"\n";
        rule2 += "  when\n";
        rule2 += "      Person(age > 18)\n";
        rule2 += "  then\n";
        rule2 += "      list.add(\"Rule2\");\n";
        rule2 += "end\n";
        
        fileManager.write( "rules.drl",
                           rule1+rule2);    
        
        this.scan(kagent);
    }
}