package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.SocialNetwork;

public record SocialNetworkResponse(
        String name,
        String url
) {
    public static SocialNetworkResponse from(SocialNetwork socialNetwork) {
        return new SocialNetworkResponse(socialNetwork.getName(), socialNetwork.getUrl());
    }
}