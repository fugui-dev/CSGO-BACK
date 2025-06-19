package com.ruoyi.domain.dto.packSack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DecomposeParam {
    private List<Long> packSackIds;
    @NotNull(message = "是否全选为必填项")
    private Boolean isAll;
}
