package com.example.integradeproject.project_management.pm_dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
//public class BoardDTO {
//    private String id;
//    private String name;
//    private OwnerDTO owner;
//
//    @Data
//    public static class OwnerDTO {
//        private String oid;
//        private String name;
//    }
//}
public class BoardDTO {
    private String id;  // This is now the uid
    private String name;
    private OwnerDTO owner;

    @Data
    public static class OwnerDTO {
        private String oid;
        private String name;

    }
}
