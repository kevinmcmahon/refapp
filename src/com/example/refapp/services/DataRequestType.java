package com.example.refapp.services;

import android.content.Context;
import com.example.refapp.R;
import com.example.refapp.models.RequestParam;
import com.example.refapp.utils.CollectionUtils;
import com.example.refapp.utils.constants.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DataRequestType {
    WEB_SEARCH(R.string.service_path_bing, null, Constants.CONTENT_TYPE_JSON, false);

    public final int servicePathResId;
    public final List<ParamType> inPathParams; // parameters for populating the value holder in the service path string
	public final String contentType;
	public final boolean isTokenRequired;

    DataRequestType(final int servicePathResId, ParamType[] paramTypeArray, String contentType, boolean isTokenRequired) {
        this.servicePathResId = servicePathResId;

        if (CollectionUtils.isEmpty(paramTypeArray)) {
            inPathParams = Collections.emptyList();
        } else {
            inPathParams = Collections.unmodifiableList(Arrays.asList(paramTypeArray));
        }

		this.contentType = contentType;
		this.isTokenRequired = isTokenRequired;
    }

    public String getServicePath(Context context) {
        return context.getString(servicePathResId);
    }

    public String getServicePath(Context context, List<RequestParam> params) {
        String[] paramStrings = new String[inPathParams.size()];

        int index = 0;
        for (ParamType eachInPathType : inPathParams) {
            paramStrings[index] = findValueByType(eachInPathType, params);
            index++;
        }

        return context.getString(servicePathResId, (Object[]) paramStrings);
    }

    static public String findValueByType(ParamType paramType, List<RequestParam> params) {
        if (CollectionUtils.isEmpty(params)) return Constants.STR_EMPTY;

        for (RequestParam eachParam : params) {
            if (paramType == eachParam.type) {
                return eachParam.value;
            }
        }
        return Constants.STR_EMPTY;
    }
}