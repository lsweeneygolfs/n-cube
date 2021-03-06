package com.cedarsoftware.ncube

import groovy.transform.CompileStatic
import org.junit.Test

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

import static com.cedarsoftware.ncube.NCubeAppContext.ncubeRuntime
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) Cedar Software LLC
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the 'License')
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an 'AS IS' BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
@CompileStatic
class TestGroovyTemplate extends NCubeBaseTest
{
    @Test
    void testDefaultConstructorIsPrivateForSerialization()
    {
        Class c = GroovyTemplate.class
        Constructor<GroovyTemplate> con = c.getDeclaredConstructor()
        assertEquals Modifier.PRIVATE, con.modifiers & Modifier.PRIVATE
        con.accessible = true
        assertNotNull con.newInstance()
    }

    @Test
    void testTemplateThatUsesClasspath()
    {
        createRuntimeCubeFromResource(ApplicationID.testAppId, 'sys.classpath.cedar.json')
        NCube template = ncubeRuntime.getNCubeFromResource(ApplicationID.testAppId, 'templateUsesClassPath.json')
        String text = template.getCell([loc:'web', testType:'templateTest'] as Map)
        assert text.contains('from the web')
        assert text.contains('Hello, world."')

        text = template.getCell([loc:'file system', testType:'templateTest'] as Map)
        assert text.contains('from the file system')
        assert text.contains('Hello, world."')
    }

    @Test
    void testNCubeGroovyExpressionThatUsesClasspath()
    {
        createRuntimeCubeFromResource(ApplicationID.testAppId, 'sys.classpath.cedar.json')
        NCube template = ncubeRuntime.getNCubeFromResource(ApplicationID.testAppId, 'templateUsesClassPath.json')
        String text = template.getCell([testType:'expressionTest'] as Map)
        assert text.contains('Hello, world."')
    }

    @Test
    void testGroovyTemplateThatUsesAtandUse()
    {
        NCube template = createRuntimeCubeFromResource(ApplicationID.testAppId, 'templateAtUseTest.json')
        Map output =[:]
        template.getCell([column:'A', row:1, text:'hello, world'] as Map, output)
        assert output.premium == 5000
        assert output.deductible == 500
        assert output.useTemplate == 'use: hello, world'
        assert output.atTemplate == 'at: hello, world'
        assert output.useTransitive == 42
        assert output.atTransitive == 16

        assert output.colCx == 'A'
        assert output.row3x == 1
        assert output.colCy == 'A'
        assert output.row4y == 1

        assert output.colBz == 'B'
        assert output.row2z == 2
        assert output.colBq == 'B'
        assert output.row3q == 3
    }
}