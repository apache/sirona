package org.apache.sirona.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class SironaServiceLoaderTest
{

    @Test
    public void use_normal_call()
        throws Exception
    {
        Iterator<BeerService> ite =
            SironaServiceLoader.load( BeerService.class, Thread.currentThread().getContextClassLoader() ).iterator();

        BeerService beerService = ite.next();

        Assert.assertNotNull( beerService );

        Assert.assertEquals( MoutainGoatBeerService.NAME, beerService.getName() );

    }

    @Test
    public void use_1_5_call()
        throws Exception
    {
        Iterator<BeerService> ite =
            SironaServiceLoader.load( BeerService.class, Thread.currentThread().getContextClassLoader() ).iterator1_5();

        BeerService beerService = ite.next();

        Assert.assertNotNull( beerService );

        Assert.assertEquals( MoutainGoatBeerService.NAME, beerService.getName() );

    }

}
