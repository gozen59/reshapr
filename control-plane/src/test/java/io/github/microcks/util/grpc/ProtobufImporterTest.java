package io.github.microcks.util.grpc;

import io.github.microcks.domain.Resource;
import io.github.microcks.domain.ResourceType;
import io.github.microcks.domain.Service;
import io.github.microcks.domain.ServiceType;
import io.github.microcks.util.MockRepositoryImportException;
import io.github.microcks.util.ReferenceResolver;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for ProtobufImporter.
 * @author laurent
 */
class ProtobufImporterTest {

   @Test
   void testProtobufWithComplexRemoteDependenciesImport() {
      ProtobufImporter importer = null;
      try {
         importer = new ProtobufImporter("target/test-classes/io/github/microcks/util/grpc/storage.proto",
               new ReferenceResolver(
                     "https://raw.githubusercontent.com/googleapis/googleapis/refs/heads/master/google/storage/v2/storage.proto",
                     null, true));
      } catch (IOException ioe) {
         fail("Exception should not be thrown");
      }

      // Check that basic service properties are there.
      List<Service> services = null;
      try {
         services = importer.getServiceDefinitions();
      } catch (MockRepositoryImportException e) {
         fail("Service definition import should not fail");
      }
      assertEquals(1, services.size());

      Service service = services.get(0);
      assertEquals("google.storage.v2.Storage", service.getName());
      assertEquals(ServiceType.GRPC, service.getType());
      assertEquals("v2", service.getVersion());
      assertEquals("google.storage.v2", service.getXmlNS());

      // Check that resources have been parsed, correctly renamed, etc...
      List<Resource> resources = null;
      try {
         resources = importer.getResourceDefinitions(service);
      } catch (MockRepositoryImportException mrie) {
         fail("Resource definition import should not fail");
      }
      assertEquals(15, resources.size());
   }

   @Test
   void testProtobufWithComplexRemoteDependenciesImport2() {
      ProtobufImporter importer = null;
      try {
         importer = new ProtobufImporter("target/test-classes/io/github/microcks/util/grpc/firestore.proto",
               new ReferenceResolver(
                     "https://raw.githubusercontent.com/googleapis/googleapis/refs/heads/master/google/firestore/v1/firestore.proto",
                     null, true));
      } catch (IOException ioe) {
         fail("Exception should not be thrown");
      }

      // Check that basic service properties are there.
      List<Service> services = null;
      try {
         services = importer.getServiceDefinitions();
      } catch (MockRepositoryImportException e) {
         fail("Service definition import should not fail");
      }
      assertEquals(1, services.size());

      Service service = services.get(0);
      assertEquals("google.firestore.v1.Firestore", service.getName());
      assertEquals(ServiceType.GRPC, service.getType());
      assertEquals("v1", service.getVersion());
      assertEquals("google.firestore.v1", service.getXmlNS());

      // Check that resources have been parsed, correctly renamed, etc...
      List<Resource> resources = null;
      try {
         resources = importer.getResourceDefinitions(service);
      } catch (MockRepositoryImportException mrie) {
         fail("Resource definition import should not fail");
      }
   }
}
