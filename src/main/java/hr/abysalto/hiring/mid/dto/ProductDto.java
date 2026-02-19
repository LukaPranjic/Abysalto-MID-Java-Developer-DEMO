package hr.abysalto.hiring.mid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Double price;
    private Double discountPercentage;
    private Double rating;
    private Integer stock;
    private List<String> tags;
    private String brand;
    private String sku;
    private Double weight;
    private DimensionsDto dimensions;
    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;
    private List<ReviewDto> reviews;
    private String returnPolicy;
    private Integer minimumOrderQuantity;
    private MetaDto meta;
    private List<String> images;
    private String thumbnail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionsDto {
        private Double width;
        private Double height;
        private Double depth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private Integer rating;
        private String comment;
        private String date;
        private String reviewerName;
        private String reviewerEmail;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaDto {
        private String createdAt;
        private String updatedAt;
        private String barcode;
        private String qrCode;
    }
}

