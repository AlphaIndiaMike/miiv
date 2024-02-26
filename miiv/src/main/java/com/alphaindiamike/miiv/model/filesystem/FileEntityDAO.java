package com.alphaindiamike.miiv.model.filesystem;

public interface FileEntityDAO {
	void save(FileEntity fileEntity);
    FileEntity findById(String id);
}
