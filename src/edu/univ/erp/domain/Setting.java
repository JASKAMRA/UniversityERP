package edu.univ.erp.domain;

import java.util.Objects;

public class Setting {
    private String key;
    private String value;

    public Setting() {}

    public Setting(String key, String value) {
        this.key=key;
        this.value=value;
    }
    //Getter fucntions
    public String getKey(){ 
        return key; 
    }
    public String getValue(){
         return value; 
    }
    //Setter functions
    public void setKey(String key){
         this.key=key; 
    }
    public void setValue(String value){ 
        this.value=value; 
    }

    @Override
    public boolean equals(Object o) {
        if (this==o){ 
            return true;
        }
        if (!(o instanceof Setting)) {
            return false;
        }    
        Setting s = (Setting) o;
        return Objects.equals(key, s.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
