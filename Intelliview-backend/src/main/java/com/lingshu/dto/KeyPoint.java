// dto/KeyPoint.java
package com.lingshu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyPoint {
    private String title;      // 例如"关键点"
    private String content;    // 例如"关键内容Key-Value格式，Key是hashCode..."
    private Integer sortOrder; // 排序
}
