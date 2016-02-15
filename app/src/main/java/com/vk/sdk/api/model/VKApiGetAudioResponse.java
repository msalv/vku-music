package com.vk.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by Kirill on 14.02.2016.
 */
public class VKApiGetAudioResponse extends VKApiModel implements Parcelable {

    /**
     * var response = API.audio.get({
     *      "offset": 0,
     *      "count": Args.count,
     *      "need_user": 1
     * });
     *
     * var user = response.items[0];
     * response.items = response.items.slice(1);
     *
     * return {
     *      "response": response,
     *      "username": user.name
     * };
     */

    /**
     * User's full name
     */
    public String username;

    /**
     * Audio array
     */
    public VKList<VKApiAudio> items;

    public VKApiGetAudioResponse() {

    }

    protected VKApiGetAudioResponse(Parcel in) {
        this.username = in.readString();
        this.items = in.readParcelable(VKList.class.getClassLoader());
    }

    /**
     * Fills an object from server response.
     */
    public VKApiGetAudioResponse(JSONObject from) {
        this.parse(from);
    }

    /**
     * Fills an object from server response.
     */
    public VKApiGetAudioResponse parse(JSONObject source) {
        JSONObject response = source.optJSONObject("response");

        this.username = response.optString("username");
        this.items = new VKList<>(response, VKApiAudio.class);

        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeParcelable(this.items, flags);
    }

    public static final Creator<VKApiGetAudioResponse> CREATOR = new Creator<VKApiGetAudioResponse>() {
        @Override
        public VKApiGetAudioResponse createFromParcel(Parcel in) {
            return new VKApiGetAudioResponse(in);
        }

        @Override
        public VKApiGetAudioResponse[] newArray(int size) {
            return new VKApiGetAudioResponse[size];
        }
    };
}
