package com.sto.transport.event.job.task.syninfo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sto.transport.event.StoTransportEventApplication;
import com.sto.transport.event.domain.syninfo.entity.BaseRoleDto;
import com.sto.transport.event.domain.syninfo.entity.StoRole;
import com.sto.transport.event.domain.syninfo.entity.UiRole;
import com.sto.transport.event.domain.syninfo.service.StoRoleService;
import com.sto.transport.event.infrastructure.util.common.HttpUtils;
import com.sto.transport.event.infrastructure.util.common.MyDateUtils;
import com.sto.transport.event.infrastructure.util.common.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SynRoleTest {

    @Value("${roleUrl}")
    private String roleUrl;
    @Value("${rolePageSize}")
    private int rolePageSize;
    @Value("${key}")
    private String appKey;
    @Value("${secret}")
    private String appSecret;

    @Autowired
    private StoRoleService stoRoleService;
    private Gson gson = new Gson();


    @Test
    public void execute() {
        try {
            log.info("【角色同步】开始执行，当前时间，" + MyDateUtils.getNowDayTime());
            //获取总条数
            int totalSize = 0;
            String success = "false";
            // 最大更新时间
            String updateTime = stoRoleService.getMaxRoleTime();

            // 13位时间戳
            long modifiedOn = MyDateUtils.getModifiedOn(updateTime);
            while (!"true".equals(success)) {
                String resp = postReqMethod(modifiedOn, 1, -1);
                JsonObject returnData = new JsonParser().parse(resp).getAsJsonObject();
                success = returnData.get("success").getAsString();
                if ("true".equals(success)) {
                    totalSize = returnData.get("data").getAsJsonObject().get("totalSize").getAsInt();
                }
            }

            int pageTotal = totalSize % rolePageSize == 0 ? totalSize / rolePageSize : totalSize / rolePageSize + 1;
            log.info("总条数：{}，总页数：{}，页大小{}", totalSize, pageTotal, rolePageSize);

            for (int currentPage = 1; currentPage <= pageTotal; currentPage++) {
                postReqMethod(modifiedOn, currentPage, pageTotal);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            log.error("同步角色信息出错=============>" + e);
        } finally {
            log.info("【角色同步】执行结束");
        }
    }

    private String postReqMethod(long modifiedOn, int currentPage, int pageTotal) {
        // 时间（yyyy-MM-dd HH:mm:ss）验签用 过期3分钟
        String timestamp = MyDateUtils.getNowDayTime();
        String sign = SignUtils.signRequest(timestamp, appSecret);
        List<NameValuePair> paramPostList = new ArrayList<>();
        paramPostList.add(new BasicNameValuePair("modifiedOn", String.valueOf(modifiedOn)));
        paramPostList.add(new BasicNameValuePair("pageNum", String.valueOf(currentPage)));
        paramPostList.add(new BasicNameValuePair("pageSize", String.valueOf(rolePageSize)));
        paramPostList.add(new BasicNameValuePair("timestamp", timestamp));
        paramPostList.add(new BasicNameValuePair("sign", sign));
        paramPostList.add(new BasicNameValuePair("appKey", appKey));
        log.info("【角色同步】入参信息=====>：modifiedOn：{} , pageNum：{}, pageSize：{} , timestamp：{} ，sign：{}， appKey:{}", MyDateUtils.getSecondsDateTime(modifiedOn), currentPage, rolePageSize, timestamp, sign, appKey);

        String resp = HttpUtils.doPost(roleUrl, paramPostList);
        log.info("【角色同步】返回信息=========>" + resp);
        if (pageTotal == -1) {
            return resp;
        }

        JsonObject returnData = new JsonParser().parse(resp).getAsJsonObject();
        String success = returnData.get("success").getAsString();
        log.info("【角色同步】总页数: {} , 当前页数: {}, 页大小: {} , 返回状态: {} ", pageTotal, currentPage, rolePageSize, success);
        if ("true".equals(success)) {
            JsonObject data = returnData.get("data").getAsJsonObject();
            BaseRoleDto dto = gson.fromJson(data, BaseRoleDto.class);
            List<UiRole> rows = dto.getRows();
            for (UiRole role : rows) {
                int roleCount = stoRoleService.countStoRoleByRoleXid(role.getCode());
                boolean flag = false;
                if (role.getCode() != null && role.getCode().startsWith("zhihui_")) {
                    flag = true;
                }
                StoRole stoRole = new StoRole();
                if (roleCount <= 0 && flag && !"1".equals(role.getDeletionStateCode())) {
                    //插入操作
                    stoRole.setRoleGid("DEFAULT." + role.getCode());
                    stoRole.setRoleXid(role.getCode());
                    stoRole.setRoleName(role.getRealName());
                    stoRole.setMenuXml("");
                    stoRole.setIsReserved(0);
                    stoRole.setDomainName("DEFAULT");
                    stoRole.setVersion(0);
                    stoRole.setInsertUser("DEFAULT.ADMIN");
                    stoRole.setInsertDate(role.getModifiedOn());
                    int rNum = stoRoleService.saveRole(stoRole);
                    log.info("新增角色数目======>" + rNum + " roleXid========>" + role.getCode() + " 角色名称====>" + role.getRealName());

                } else if (roleCount > 0 && flag && !"1".equals(role.getDeletionStateCode())) {
                    //更新操作
                    stoRole.setRoleGid("DEFAULT." + role.getCode());
                    stoRole.setRoleXid(role.getCode());
                    stoRole.setRoleName(role.getRealName());
                    stoRole.setUpdateUser("DEFAULT.ADMIN");
                    stoRole.setUpdateDate(role.getModifiedOn());
                    int uNum = stoRoleService.updateRole(stoRole);
                    log.info("更新角色数目======>" + uNum + " roleXid========>" + role.getCode() + " 角色名称====>" + role.getRealName());

                } else {
                    //删除操作
                    if (roleCount > 0 && flag && "1".equals(role.getDeletionStateCode())) {
                        int dNum = stoRoleService.deleteByCode(role.getCode());
                        log.info("删除角色数目======>" + dNum + " roleXid========>" + role.getCode() + " 角色名称====>" + role.getRealName());

                    }
                }

            }
        }

        return resp;
    }


}
