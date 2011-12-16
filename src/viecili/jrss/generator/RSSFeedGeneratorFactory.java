/*
 * Created on Jul 30, 2004 by viecili
 */
package viecili.jrss.generator;

/**
 * Factory class to get an implementation of <code>RSSFeedGenerator</code> interface
 * 
 * @author Henrique A. Viecili
 */
public class RSSFeedGeneratorFactory {

    /** Gets a default implementation of RSSFeedGenerator 
     * @see RSSFeedGeneratorImpl
     * @return Returns the default RSSFeedGeneratorImpl
     */
    public static RSSFeedGenerator getDefault() {
        return new RSSFeedGeneratorImpl();
    }
    /** Gets an user implemented RSSFeedGenerator 
     * @param className The class name of the class that implements RSSFeedGenerator
     * @return Returns the RSSFeedGenerator specific implementation or the default if any exception occur
     */
    public static RSSFeedGenerator getInstance(String className) {
        try {
            return (RSSFeedGenerator) Class.forName(className).newInstance();
        } catch (Exception e) {
            return getDefault();
        }
    }
    
}
