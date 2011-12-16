/*
 * Created on Jul 30, 2004 by viecili
 */
package viecili.jrss.generator;

import java.io.File;
import java.io.OutputStream;

import viecili.jrss.generator.elem.RSS;

/**
 * The interface definition for RSS Feed Generators.
 * 
 * @author Henrique A. Viecili
 */
public interface RSSFeedGenerator {
    
    /** Generate the RSS Feed content to the specified file.
     * @param rss The RSS Feed content
     * @param xmlFile The output file
     */
    void generateToFile(RSS rss, File xmlFile) throws Exception;
    
    /** Generate the RSS Feed xml content and return it as String
     * @param rss The RSS Feed content
     * @return the xml as string
     */
    String generateAsString(RSS rss) throws Exception;
    
    /** Generate the RSS Feed xml content to the specified stream
     * @param rss The RSS Feed content
     * @param outs the stream containing the results
     */
    public void generateToStream(RSS rss, OutputStream outs) throws Exception;
}