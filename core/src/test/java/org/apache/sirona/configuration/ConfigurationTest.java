/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ConfigurationTest {

    @Test
    public void isTrue(){
        Assert.assertEquals( true, Configuration.is( "org.apache.sirona.goodbeer", false ) );
    }

    @Test
    public void isFalse(){
        Assert.assertEquals( false, Configuration.is( "org.apache.sirona.badbeer", false ) );
    }

    @Test
    public void getArray(){
        //foo;bar;beer
        String[] array = Configuration.getArray( "org.apache.sirona.ehcache.methods", null );
        Assert.assertEquals( 3, array.length );
        Assert.assertEquals( "foo", array[0] );
        Assert.assertEquals( "bar", array[1] );
        Assert.assertEquals( "beer", array[2] );
    }
}
