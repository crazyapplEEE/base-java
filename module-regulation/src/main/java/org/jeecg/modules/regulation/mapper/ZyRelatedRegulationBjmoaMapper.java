package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;

import java.util.List;

/**
 * @author Ran Meng
 * @date 2023-09-22
 */
@Mapper public interface ZyRelatedRegulationBjmoaMapper extends BaseMapper<ZyRelatedRegulationBjmoa> {
    ZyRelatedRegulationBjmoa queryByRegulationIdentifiers(@Param("regulationIdentifierA") String regulationIdentifierA,
                                                     @Param("regulationIdentifierB") String regulationIdentifierB);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifier(@Param("regulationIdentifier") String regulationIdentifier);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersion(@Param("regulationIdentifier") String regulationIdentifier,
                                                                         @Param("regulationVersion") String regulationVersion);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersionAndCode(@Param("regulationIdentifier") String regulationIdentifier,
                                                                                @Param("regulationVersion") String regulationVersion,
                                                                                @Param("regulationCode") String regulationCode);

    List<ZyRelatedRegulationBjmoa> queryInternalRelatedeRegulations(@Param("regulationIdentifierA") String regulationIdentifierA,
                                                                    @Param("regulationVersionA") String regulationVersionA,
                                                                    @Param("regulationCodeA") String regulationCodeA,
                                                                    @Param("regulationIdentifierB") String regulationIdentifierB,
                                                                    @Param("regulationVersionB") String regulationVersionB,
                                                                    @Param("regulationCodeB") String regulationCodeB);

    List<ZyRelatedRegulationBjmoa> queryExternalRelatedeRegulations(@Param("regulationIdentifierA") String regulationIdentifierA,
                                                                    @Param("regulationVersionA") String regulationVersionA,
                                                                    @Param("regulationCodeA") String regulationCodeA,
                                                                    @Param("regulationIdentifierB") String regulationIdentifierB);

    int deleteByRegulationIdentifier(@Param("regulationIdentifier") String regulationIdentifier);

    void truncateTable();
}
