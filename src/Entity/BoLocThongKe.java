package Entity;

import java.time.LocalDateTime;
import java.sql.Date;
import java.util.Objects;

public class BoLocThongKe {
    private String maNV;
    private Integer ca;
    private LocalDateTime startTime;
    private String modeLocThoiGian; // "THANG", "QUY", "TUYCHINH"
    private Integer month;
    private Integer quarter;
    private Integer year;
    private Date fromDate;
    private Date toDate;

    public BoLocThongKe() {}

    public BoLocThongKe(String maNV, Integer ca, LocalDateTime startTime, String modeLocThoiGian,
                        Integer month, Integer quarter, Integer year, Date fromDate, Date toDate) {
        this.maNV = maNV;
        this.ca = ca;
        this.startTime = startTime;
        this.modeLocThoiGian = modeLocThoiGian;
        this.month = month;
        this.quarter = quarter;
        this.year = year;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public Integer getCa() { return ca; }
    public void setCa(Integer ca) { this.ca = ca; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public String getModeLocThoiGian() { return modeLocThoiGian; }
    public void setModeLocThoiGian(String modeLocThoiGian) { this.modeLocThoiGian = modeLocThoiGian; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Date getFromDate() { return fromDate; }
    public void setFromDate(Date fromDate) { this.fromDate = fromDate; }

    public Date getToDate() { return toDate; }
    public void setToDate(Date toDate) { this.toDate = toDate; }

    @Override
    public int hashCode() {
        return Objects.hash(maNV, ca, startTime, modeLocThoiGian, month, quarter, year, fromDate, toDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoLocThongKe other = (BoLocThongKe) obj;
        return Objects.equals(maNV, other.maNV)
                && Objects.equals(ca, other.ca)
                && Objects.equals(startTime, other.startTime)
                && Objects.equals(modeLocThoiGian, other.modeLocThoiGian)
                && Objects.equals(month, other.month)
                && Objects.equals(quarter, other.quarter)
                && Objects.equals(year, other.year)
                && Objects.equals(fromDate, other.fromDate)
                && Objects.equals(toDate, other.toDate);
    }

    @Override
    public String toString() {
        return "BoLocThongKe [maNV=" + maNV + ", ca=" + ca + ", startTime=" + startTime
                + ", modeLocThoiGian=" + modeLocThoiGian + ", month=" + month
                + ", quarter=" + quarter + ", year=" + year
                + ", fromDate=" + fromDate + ", toDate=" + toDate + "]";
    }
}