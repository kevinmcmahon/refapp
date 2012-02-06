package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.refapp.services.ParamType;
import com.example.refapp.utils.ParcelUtils;

public class RequestParam implements Parcelable {
	public ParamType type;
	public String value;

	public RequestParam() {}

	public RequestParam(ParamType type, String value) {
		this.type = type;
		this.value = value;
	}

	public RequestParam(Parcel src) {
		type = ParcelUtils.readEnum(src, ParamType.class);
		value = src.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		ParcelUtils.writeEnum(dest, type);
		dest.writeString(value);
	}

	public static final Parcelable.Creator<RequestParam> CREATOR = new Parcelable.Creator<RequestParam>() {
		public RequestParam createFromParcel(Parcel in) {
			return new RequestParam(in);
		}

		public RequestParam[] newArray(int size) {
			return new RequestParam[size];
		}
	};
}
