package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaCarouselPictureService;
import org.jeecg.modules.regulation.vo.ZyCarouselPictureVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ran Meng
 * @date 2023-09-25
 */
@Service
@Slf4j
public class ZyRegulationBjmoaCarouselPictureServiceImpl implements IZyRegulationBjmoaCarouselPictureService {

    @Value("${biisaas.bjmoaRegulationInfo.applicationId}")
    private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.carouselPictureId}")
    private String bjmoaRegulationCarouselPictureInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.bjmoaCarouselPictureRedisKey}")
    private String bjmoaCarouselPictureRedisKey;

    @Autowired
    private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("bjmoaContentManagementService")
    private IContentManagementService contentManagementService;

    @Override
    public Result<?> carouselPicture() {
        // 先在redis里面看看有没有
        final String redisKey = bjmoaCarouselPictureRedisKey;
        final Object bjmoaCarouselPictureObj = redisUtil.get(redisKey);
        if (!ObjectUtils.isEmpty(bjmoaCarouselPictureObj)) {
            return Result.OK(bjmoaCarouselPictureObj);
        }

        // 1. 查询轮播图基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationCarouselPictureInfoFormModelId);

        final JSONObject record = qiqiaoFormsService.page(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND REGULATION RECORD");
            return null;
        }
        //2. 存所有“图片排序”不为0的对象进入list
        final JSONArray carouselPictureList = record.getJSONArray("list");
        List<ZyCarouselPictureVO> result = new ArrayList<>(carouselPictureList.size());
        for (int i = 0; i < carouselPictureList.size(); i++) {
            final JSONObject carouselPicture = carouselPictureList.getJSONObject(i);
            final ZyCarouselPictureVO carouselPictureVO = new ZyCarouselPictureVO();
            final JSONObject carouselPictureInfo = carouselPicture.getJSONObject("variables");
            carouselPictureVO.setTitle(carouselPictureInfo.getString("名称"));
            carouselPictureVO.setPictureUrl(carouselPictureInfo.getString("链接"));
            final String fileId = carouselPictureInfo.getString("内管文件编号");
            final String pictureUrl = contentManagementService.getDownloadUrl(fileId);
            carouselPictureVO.setPicture(pictureUrl);
            final Integer pictureOrder = carouselPictureInfo.getInteger("图片排序");
            carouselPictureVO.setOrder(pictureOrder);
            if (pictureOrder != 0) {
                result.add(carouselPictureVO);
            }

        }
        //3.根据图片顺序进行排序
        Collections.sort(result, new Comparator<ZyCarouselPictureVO>() {
            @Override
            public int compare(ZyCarouselPictureVO o1, ZyCarouselPictureVO o2) {
                if (o1.getOrder() - o2.getOrder() > 0) {
                    return 1;
                } else if (o1.getOrder() - o2.getOrder() < 0) {
                    return -1;
                } else {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            }
        });
        if (!CollectionUtils.isEmpty(result)) {
            redisUtil.del(redisKey);
            redisUtil.set(redisKey, result, 60);
        }
        return Result.OK(result);
    }

}
