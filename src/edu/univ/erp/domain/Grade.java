package edu.univ.erp.domain;


import java.util.Objects;

/**
 * Grade component for an enrollment.
 * component could be "midterm", "assignment1", "final" etc.
 */
public class Grade {
    private Integer gradeId;
    private Integer enrollmentId;
    private String component;
    private Double score;
    private String finalGrade; // e.g., "A", "B+"
   

    public Grade() {}

    public Grade(Integer gradeId, Integer enrollmentId, String component, Double score, String finalGrade) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
   
    }

    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }

    public Integer getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Integer enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }

   

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", component='" + component + '\'' +
                ", score=" + score +
                ", finalGrade='" + finalGrade + 
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grade)) return false;
        Grade g = (Grade) o;
        return Objects.equals(gradeId, g.gradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeId);
    }
}

