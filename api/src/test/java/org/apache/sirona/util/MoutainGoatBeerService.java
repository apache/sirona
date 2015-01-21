package org.apache.sirona.util;


/**
 * @author Olivier Lamy
 */
public class MoutainGoatBeerService
    implements BeerService
{
    public static final String NAME = "I'm the best Aussie beer";

    public String getName()
    {
        return NAME;
    }
}
