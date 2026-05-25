package com.lingshu.service;

import com.lingshu.dto.response.JobRoleConfigResponse;
import com.lingshu.dto.response.JobRoleOptionResponse;

import java.util.List;

public interface JobRoleService {
    List<JobRoleOptionResponse> listActiveRoles();

    JobRoleConfigResponse getRoleConfig(String code);
}
