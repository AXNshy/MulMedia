//
// Created by Luffy on 2023/4/18.
//

#ifndef MULMEDIA_RENDERSTATE_H
#define MULMEDIA_RENDERSTATE_H

enum RenderState {
    NO_SURFACE,
    FRESH_SURFACE,
    SURFACE_CHANGE,
    RENDERING,
    SURFACE_DESTROY,
    STOP
};

#endif //MULMEDIA_RENDERSTATE_H


