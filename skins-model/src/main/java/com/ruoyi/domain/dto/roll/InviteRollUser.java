package com.ruoyi.domain.dto.roll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InviteRollUser {

    @NotNull(message = "rollId不能为空")
    private Integer rollId;

    @NotEmpty
    private List<Integer> userIds;

}
