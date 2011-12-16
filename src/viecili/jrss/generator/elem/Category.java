/*
 * Created on Jul 30, 2004 by viecili
 */
package viecili.jrss.generator.elem;

/**
 * Class to define the <code>&lt;category&gt;</code> element.
 * <p>It has one optional attribute, domain, a string that identifies a categorization taxonomy.</p> 
 * <p>The value of the element is a forward-slash-separated string that identifies a hierarchic location 
 * in the indicated taxonomy. Processors may establish conventions for the interpretation of categories.</p> 
 * @author Henrique A. Viecili
 */
public abstract class Category {
    String name;
    String domain;
    
    /** The Category constructor with the required param.
     * @exception InvalidRequiredParamException if some parameter passed is invalid
     * @param name The name of the category
     */
    public Category(String name) {
        this(name,null);
    }
    
    /** The Category constructor specifying a domain.
     * @exception InvalidRequiredParamException if some parameter passed is invalid
     * @param name The name of the category
     * @param domain The domain URL
     */
    public Category(String name, String domain) {
        super();
        
        if(name == null || "".equals(name.trim()))
            throw new InvalidRequiredParamException("name required: "+name);
        
        this.name = name;
        this.domain = domain;
    }
    
    /** Gets the category domain
     * @return Returns the domain.
     */
    public String getDomain() {
        return domain;
    }
    /** Sets the category domain
     * @param domain The domain to set.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }
    /** Gets the category name
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /** Sets the category name
     * @exception InvalidRequiredParamException if some parameter passed is invalid
     * @param name The name to set.
     */
    public void setName(String name) {
        if(name == null || "".equals(name.trim()))
            throw new InvalidRequiredParamException("name required: "+name);
        this.name = name;
    }
}