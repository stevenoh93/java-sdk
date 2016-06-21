/**
 * Copyright 2015 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ibm.watson.developer_cloud.natural_language_classifier.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;

import com.ibm.watson.developer_cloud.WatsonServiceTest;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier.Status;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifiers;
import com.ibm.watson.developer_cloud.service.exception.NotFoundException;

/**
 * The Class NaturalLanguageClassifierTest.
 */
public class NaturalLanguageClassifierIT extends WatsonServiceTest {

  /** The classifier id. */
  private String classifierId;

  /** The service. */
  private NaturalLanguageClassifier service;

  /**
   * Creates the classifier.
   *
   * @throws Exception the exception
   */
  @Test
  public void createAndGetStatusForClassifier() throws Exception {
    final File trainingData =
        new File("src/test/resources/natural_language_classifier/weather_data_train.csv");

    final String name = "itest-example";
    Classifier classifier = service.createClassifier(name, "en", trainingData).execute();

    try {
      assertNotNull(classifier);
      assertEquals(Status.TRAINING, classifier.getStatus());
      assertEquals(name, classifier.getName());

      Thread.sleep(2000L);
      classifier = service.getClassifier(classifier.getId()).execute();

      assertNotNull(classifier);
      assertEquals(name, classifier.getName());
    } finally {
      service.deleteClassifier(classifier.getId());
    }

  }

  /**
   * Test delete all classifiers.
   */
  @Test
  public void testDeleteAllClassifiers() {
    List<Classifier> classifiers = service.getClassifiers().execute().getClassifiers();
    for (Classifier classifier : classifiers) {
      if (!classifier.getId().equals(classifierId)) {
        service.deleteClassifier(classifier.getId()).execute();
      }
    }
  }
  
  /**
   * Test classify.
   */
  @Test
  public void testClassify() {
    final Classification classification;

    try {
      classification = service.classify(classifierId, "is it hot outside?").execute();
    } catch (NotFoundException e) {
      // #324: Classifiers may be empty, because of other tests interfering.
      // The build should not fail here, because this is out of our control.
      throw new AssumptionViolatedException(e.getMessage(), e);
    }

    assertNotNull(classification);
    assertEquals("temperature", classification.getTopClass());
  }

  /*
   * (non-Javadoc)
   *
   * @see com.ibm.watson.developer_cloud.WatsonServiceTest#setUp()
   */
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = new NaturalLanguageClassifier();
    service.setDefaultHeaders(getDefaultHeaders());
    service.setUsernameAndPassword(getValidProperty("natural_language_classifier.username"),
        getValidProperty("natural_language_classifier.password"));
    service.setEndPoint(getValidProperty("natural_language_classifier.url"));
    classifierId = getValidProperty("natural_language_classifier.classifier_id");
  }

  /**
   * Test get classifier.
   */
  @Test
  public void testGetClassifier() {
    final Classifier classifier;

    try {
      classifier = service.getClassifier(classifierId).execute();
    } catch(NotFoundException e) {
      // #324: Classifiers may be empty, because of other tests interfering.
      // The build should not fail here, because this is out of our control.
      throw new AssumptionViolatedException(e.getMessage(), e);
    }
    assertNotNull(classifier);
    assertEquals(classifierId, classifier.getId());
  }

  /**
   * Test get classifiers.
   */
  @Test
  public void testGetClassifiers() {
    final Classifiers classifiers = service.getClassifiers().execute();
    assertNotNull(classifiers);

    // #324: Classifiers may be empty, because of other tests interfering.
    // The build should not fail here, because this is out of our control.
    Assume.assumeFalse(classifiers.getClassifiers().isEmpty());
  }
}
