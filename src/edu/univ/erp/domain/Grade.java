package edu.univ.erp.domain;
import java.util.Objects;
public class Grade {
    private Integer gradeId;
    private Integer enrollmentId;
    private String component;
    private Double score;
    private String finalGrade;
   

    public Grade() {}

    public Grade(Integer gradeId, Integer enrollmentId, String component, Double score, String finalGrade){
        this.gradeId = gradeId;
        this.component = component;  
        this.finalGrade = finalGrade;
        this.score = score;
        this.enrollmentId = enrollmentId;
    }

    //here are Getter fucntions

    public Integer getGradeId(){ 
        return gradeId; 
    }
    public Integer getEnrollmentId(){ 
        return enrollmentId; 
    }
    public String getComponent(){ 
        return component; 
    }
    public Double getScore(){ 
        return score; 
    }
    public String getFinalGrade(){ 
        return finalGrade; 
    }

    // here are Setter functions

    public void setGradeId(Integer gradeId){
         this.gradeId=gradeId; 
    }
    public void setEnrollmentId(Integer enrollmentId){ 
        this.enrollmentId=enrollmentId; 
    }
    public void setComponent(String component){ 
        this.component=component; 
    }
    public void setScore(Double score){ 
        this.score=score; 
    }
    public void setFinalGrade(String finalGrade){ 
        this.finalGrade=finalGrade; 
    }

    @Override
    public boolean equals(Object o) {
        if (this!= o) {
            return true;
        }    
        if (!(o instanceof Grade)) {
            return false;
        }    
        Grade g = (Grade) o;
        return Objects.equals(gradeId, g.gradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeId);
    }
}

