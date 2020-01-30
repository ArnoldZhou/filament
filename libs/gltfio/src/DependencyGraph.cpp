/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "DependencyGraph.h"

using namespace filament;
using namespace utils;

namespace gltfio {

Entity DependencyGraph::popReadyRenderable() noexcept {
    if (mReadyRenderables.empty()) {
        return Entity();
    }
    auto top = mReadyRenderables.top();
    mReadyRenderables.pop();
    return top;
}

void DependencyGraph::addEdge(Entity entity, MaterialInstance* mi) {
    assert(!mFinalized);
    mMaterialToEntity[mi].insert(entity);
    mEntityToMaterial[entity].materials.insert(mi);
}

void DependencyGraph::addEdge(MaterialInstance* mi, const char* name) {
    assert(!mFinalized);
    mMaterialToTexture[mi].params[name] = {};
}

void DependencyGraph::finalize() {
    assert(!mFinalized);
    for (auto pair : mEntityToMaterial) {
        if (pair.second.materials.empty()) {
            mReadyRenderables.push(pair.first);
        }
    }
    mFinalized = true;
}

void DependencyGraph::addEdge(Texture* texture, MaterialInstance* mi, const char* name) {
    assert(mFinalized);
    assert(mTextureToMaterial.find(texture) != mTextureToMaterial.end());
    assert(mMaterialToTexture.find(mi) != mMaterialToTexture.end());
    assert(mMaterialToTexture[mi].params.find(name) != mMaterialToTexture[mi].params.end());
    mTextureToMaterial[texture].insert(mi);
    //mMaterialToTexture[mi].params[name].texture = texture;
}

void DependencyGraph::markAsReady(Texture* texture) {
    assert(mFinalized);
    assert(mTextureToMaterial.find(texture) != mTextureToMaterial.end());
    auto& materials = mTextureToMaterial[texture];
    for (auto material : materials) {
        assert(mMaterialToTexture.find(material) != mMaterialToTexture.end());
        auto& status = mMaterialToTexture[material];
        bool ready = true;
        for (auto iter = status.params.begin(); iter != status.params.end(); ++iter) {
            // if (iter->second.texture == texture) {
            //     iter.value().ready = true;
            // }
            // if (!iter->second.ready) {
            //     ready = false;
            // }
        }
        if (ready && ++status.numReady == status.params.size()) {
            markAsReady(material);
        }
    }
}

void DependencyGraph::markAsReady(MaterialInstance* material) {
    assert(mMaterialToTexture.find(material) != mMaterialToTexture.end());
    auto& entities = mMaterialToEntity[material];
    for (auto entity : entities) {
        assert(mEntityToMaterial.find(entity) != mEntityToMaterial.end());
        auto& status = mEntityToMaterial[entity];
        if (++status.numReady == status.materials.size()) {
            mReadyRenderables.push(entity);
        }
    }
}

} // namespace gltfio
