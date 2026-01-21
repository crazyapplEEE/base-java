package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.qiqiao.constants.RecordVO;

import java.util.List;

public interface IQiqiaoFormsService {
    JSONObject queryById(RecordVO recordVO);

    JSONObject page(RecordVO recordVO);

    boolean deleteById(RecordVO recordVO);

    /**
     * 插入数据
     *
     * @param recordVO
     * @return
     */
    JSONObject saveOrUpdate(RecordVO recordVO);

    /**
     * 批量新增数据
     *
     * @param recordVOList
     * @return
     */
    boolean batchSave(List<RecordVO> recordVOList);

    /**
     * 批量更新数据
     *
     * @param recordVOList
     * @return
     */
    boolean batchUpdate(List<RecordVO> recordVOList);

    /**
     * 文件上传
     *
     * @param recordVO
     * @return
     */
    JSONArray upload(RecordVO recordVO);

    /**
     * 文件下载
     *
     * @param recordVO
     */
    void download(RecordVO recordVO, String outFilePath);

    /**
     * 获取文件下载地址
     *
     * @param recordVO
     * @return
     */
    String getDownloadUrl(RecordVO recordVO);
}
