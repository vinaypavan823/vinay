package com.terralogic.hros.lms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.terralogic.hros.lms.entity.RequestContent;
@Repository
public interface RequestContentRepo extends MongoRepository<RequestContent,String>{


	//@Query("{'url':?0}")
	RequestContent findByUrl(String url);



}
