package com.smartattendance.controller.student;

public class StudentModuleSummary {
    private final String module;
    private final int attended;
    private final int total;
    private final double ratio;

    public StudentModuleSummary(String module, int attended, int total, double ratio) {
        this.module = module;
        this.attended = attended;
        this.total = total;
        this.ratio = ratio;
    }

    public String getModule() { return module; }
    public int getAttended()  { return attended; }
    public int getTotal()     { return total; }
    public Double getRatio()  { return ratio; }
}
