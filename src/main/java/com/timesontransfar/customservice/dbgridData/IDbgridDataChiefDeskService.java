package com.timesontransfar.customservice.dbgridData;

import java.util.List;
@SuppressWarnings("rawtypes")
public interface IDbgridDataChiefDeskService {
	
	GridDataInfo querySheetStatuApply(int begin,String strWhere);
	
	GridDataInfo queryPatch(int begin,String strWhere);
	
	GridDataInfo queryChiefDesk(int begin, String strWhere);
	
	List query4Dispatch(String where);

	List queryPatchdsp(String where, int num);

}
