/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink2.models;

import com.orm.SugarRecord;
import java.util.Date;

public class PhotoModel extends SugarRecord<PhotoModel> {


    private Long id;
    private Integer mode;     // camera 0 / dsrl 1/video 2
    private String fullpath;  //원본파일패스
    private String filename;
    private String thumbpath;  //섬네일 파일패스
   // private String targetName;  // 사용하지 않음
   // private String targetId;    //사용하지 않음
    private Boolean uploaded = false;     //업로드 완료시 , 모든 업로드 완료시 true
    private Integer uploading = 0;        //초기값 0, 업로드중: 1, 업로드 완료: 2, 업로드 실패: 3
    private Integer thumbUploading = 0;
    private Integer chainUploading = 0;
    //private Boolean needUploading = false;
    private Date created;






    public PhotoModel() {

    }

    /**
     * 리스트업시 사용하려고??
     * @return
     */
    public String getFullpath() {
        return fullpath;
    }

    public void setFullpath(String fullpath) {
        this.fullpath = fullpath;
    }

    public String getThumbpath(){
        return thumbpath;
    }

    public void setThumbpath(String thumbpath){
        this.thumbpath = thumbpath;
    }
//
//    public void setId(long id) {
//        this.id = id;
//    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filname) {
        this.filename = filname;
    }

//    public String getTargetName() {
//        return targetName;
//    }
//
//    public void setTargetName(String targetName) {
//        this.targetName = targetName;
//    }
//
//    public String getTargetId() {
//        return targetId;
//    }
//
//    public void setTargetId(String targetId) {
//        this.targetId = targetId;
//    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

//    public Boolean getNeedUploading() {
//        return needUploading;
//    }
//
//    public void setNeedUploading(Boolean needUploading) {
//        this.needUploading = needUploading;
//    }

    public Integer getUploading() {
        return uploading;
    }

    public void setUploading(Integer uploading) {
        this.uploading = uploading;
    }


    public Integer getThumbUploading() {
        return thumbUploading;
    }

    public void setThumbUploading(Integer thumbUploading) {
        this.thumbUploading = thumbUploading;
    }

    public Integer getChainUploading() {
        return chainUploading;
    }

    public void setChainUploading(Integer chainUploading) {
        this.chainUploading = chainUploading;
    }


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

//    @Override
//    public Long getId() {
//        return id;
//    }
//
//    @Override
//    public void setId(Long id) {
//        this.id = id;
//    }



}
