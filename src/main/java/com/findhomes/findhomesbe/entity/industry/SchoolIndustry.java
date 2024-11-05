package com.findhomes.findhomesbe.entity.industry;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="school_industry_tbl")
public class SchoolIndustry implements Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer schoolIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;
    private Point coordinate;

}

