/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugtree.training.drools;

import java.util.Iterator;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ByteArrayResource;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author esteban
 */
public class BasicTest {
    
    public BasicTest() {
    }

    @Test
    public void test() {
        
        String rule = "";
        rule += "package org.drools\n";

        rule += "rule \"Rule 1\"\n";
        rule += "    when\n";
        rule += "        Person ($var: pets )\n";
        rule += "        Pet () from $var\n";
        rule += "        not Pet ()\n";
        rule += "    then\n";
        rule += "       System.out.println(\"Fire in the hole\");\n";
        rule += "end\n";
        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ByteArrayResource(rule.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()){
            Iterator<KnowledgeBuilderError> errors = kbuilder.getErrors().iterator();
            
            while(errors.hasNext()){
                System.out.println("kbuilder error: "+errors.next().getMessage());
            }
        }
        
        assertFalse(kbuilder.hasErrors());
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        
    }
}
