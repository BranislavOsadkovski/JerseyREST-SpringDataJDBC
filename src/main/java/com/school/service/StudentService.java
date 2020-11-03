package com.school.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.util.Assert;

import com.school.enumerations.OcupationType;
import com.school.factories.AbstractFactory;
import com.school.factories.OcupationFactory;
import com.school.interfaces.Ocupation;
import com.school.objects.Image;
import com.school.objects.Student;
import com.school.proxyimage.ProxyImage;
import com.school.util.StudentJDBCTemplate;
import com.school.validations.StudentException;
import com.school.validations.StudentValidator;

/**
 * StudentService class is a Web Service class and its methods receive
 * HttpRequests on paths mapped by javax.ws.rs.Path annotations and send
 * HttpResponse to client after they are processed
 * 
 * @author Branislav
 *
 */
@Singleton
@Path(value = "StudentService")
public class StudentService {
	private Student student;
	private List<Student> list;
	private @Inject HttpServletRequest request;
	private @Inject HttpServletResponse response;
	private StudentJDBCTemplate studentTemplate;
	private final AbstractFactory factory = new OcupationFactory();
	final static Logger logger = Logger.getLogger(StudentService.class);

	/**
	 * Responds to HttpPOST requests in MULTIPART_FORM_DATA MediaType form.
	 * Validates parameters from the Request if invalid throws StudentException.
	 * Creates Student object and saves it into database.
	 * 
	 * @param name
	 * @param age
	 * @param email
	 * @param stream
	 * @return Response
	 */
	@POST
	@Path(value = "newstudent")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response createStudent(@FormDataParam(value = "name") String name, @FormDataParam(value = "age") String age,
			@FormDataParam(value = "email") String email, @FormDataParam(value = "image") InputStream stream) {
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			Ocupation student = (Student) factory.getOcupation(OcupationType.STUDENT);

			if (StudentValidator.validateStudent(name, age, email)) {
				student.setName(name);
				student.setAge(Integer.valueOf(age));
				student.setEmail(email);
				student.setImage(imageBytes(stream));
				studentTemplate.create(student);
			}
		} catch (StudentException e) {
			logger.error(e.getMessage(), e);
			try {
				response.sendRedirect("/school/index.jsp");
			} catch (IOException io) {
				logger.error(io.getMessage(), io);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return Response.ok().build();
	}

	/**
	 * Responds to HttpGET requests. Validates parameters from the Request if
	 * invalid throws StudentException. Returns Student record from database.
	 * Returns Response in APPLICATION_XML MediaType form.
	 * 
	 * @param id
	 * @return Response
	 */
	@GET
	@Path(value = "student/{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getStudent(@PathParam(value = "id") String id) {
		student = null;
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			if (StudentValidator.validateId(id)) {
				student = studentTemplate.getById(Integer.valueOf(id));
			}
		} catch (StudentException se) {
			logger.error(se.getMessage(), se);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (student != null) {
			return Response.ok(student).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Responds to HttpGET requests. Validates parameters from the Request if
	 * invalid throws StudentException. Returns Student record from database.
	 * Returns Response in APPLICATION_XML MediaType form.
	 * 
	 * @param name
	 * @return Response
	 */
	@GET
	@Path(value = "student/name/{name}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getStudentByName(@PathParam(value = "name") String name) {
		student = null;
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			if (StudentValidator.validatePathName(name)) {
				student = studentTemplate.getByName(name);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (student != null) {
			return Response.ok(student).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Responds to HttpPUT requests in MULTIPART_FORM_DATA MediaType form. Validates
	 * parameters from the Request if invalid throws StudentException. Updates
	 * Student record in database.
	 * 
	 * @param id
	 * @param name
	 * @param age
	 * @param email
	 * @param stream
	 * @return Response
	 */
	@PUT
	@Path(value = "student")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response updateStudent(@FormDataParam(value = "id") String id, @FormDataParam(value = "name") String name,
			@FormDataParam(value = "age") String age, @FormDataParam(value = "email") String email,
			@FormDataParam(value = "image") InputStream stream) {
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			Ocupation student = factory.getOcupation(OcupationType.STUDENT);

			if (StudentValidator.validateStudent(id, name, age, email)) {
				student.setId(Integer.valueOf(id));
				student.setAge(Integer.valueOf(age));
				student.setEmail(email);
				student.setName(name);

				studentTemplate.update(student);
			}
		} catch (StudentException se) {
			logger.error(se.getMessage(), se);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return Response.ok().build();
	}

	/**
	 * Responds to HttpDELETE requests in MULTIPART_FORM_DATA MediaType form.
	 * Validates parameters from the Request if invalid throws StudentException.
	 * Deletes Student record from database.
	 * 
	 * @param id
	 * @return Response
	 */
	@DELETE
	@Path(value = "student")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response deleteStudent(@FormDataParam(value = "id") String id) {
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			if (StudentValidator.validateId(id)) {

				studentTemplate.delete(Integer.valueOf(id));
			}
		} catch (StudentException se) {
			logger.error(se.getMessage(), se);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return Response.ok().build();
	}

	/**
	 * Responds to HttpGET requests. Validates parameters from the Request if
	 * invalid throws StudentException. Returns image record from database. Returns
	 * Response in APPLICATION_OCTET_STREAM MediaType form.
	 * 
	 * @param id
	 * @return Response
	 */
	@GET
	@Path(value = "{id}/studentimage")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStudentImage(@PathParam(value = "id") String id) {
		Response r = null;

		byte[] imageBytes = new byte[1024];

		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");
			if (StudentValidator.validateId(id)) {

				Image img = ProxyImage.getProxyImage(Integer.valueOf(id), studentTemplate);

				imageBytes = img.getImageBytes();

				BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
				if (imageBytes == null) {
					r = Response.status(Response.Status.NO_CONTENT).build();
				} else if (imageBytes != null && imageBytes.length > 0) {
					r = Response.ok(imageBytes, MediaType.APPLICATION_OCTET_STREAM_TYPE).build();
					bos.write(imageBytes);
				}
			}
		} catch (StudentException se) {
			logger.error(se.getMessage(), se);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return r;
	}

	/**
	 * Responds to HttpPUT requests in MULTIPART_FORM_DATA MediaType form. Validates
	 * parameters from the Request if invalid throws StudentException. Updates image
	 * record in database.
	 * 
	 * @param id
	 * @param stream
	 * @return Response
	 */
	@PUT
	@Path(value = "studentimage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response setStudentImage(@FormDataParam(value = "id") String id,
			@FormDataParam(value = "image") InputStream stream) {
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");
			byte[] imageBytes = imageBytes(stream);
			if (StudentValidator.validateId(id)) {
				if (imageBytes.length > 0) {
					studentTemplate.setImage(Integer.valueOf(id), imageBytes);
				} else {
					throw new NullPointerException("No image found;");
				}
			}
		} catch (StudentException se) {
			logger.error(se.getMessage(), se);
		} catch (NullPointerException npe) {
			logger.error(npe.getMessage(), npe);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return Response.ok().build();
	}

	/**
	 * Responds to HttpGET requests. Validates parameters from the Request if
	 * invalid throws StudentException. Returns Student record from database. Fetch
	 * a list of all Student records from database in xml form. Returns Response in
	 * APPLICATION_XML MediaType form.
	 * 
	 * @return list
	 */
	@GET
	@Path(value = "students")
	@Produces(MediaType.APPLICATION_XML)
	public List<Student> getAllStudents() {
		try {
			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");

			list = studentTemplate.getAllRecords();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;

	}

	/**
	 * Responds to HttpPUT requests in MULTIPART_FORM_DATA MediaType form. Validates
	 * parameters from the Request if invalid throws StudentException. Inserts a
	 * large list of records as batch update into database
	 * 
	 * 
	 * @param batch
	 * @return Response
	 */
	@PUT
	@Path(value = "students")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response batchUpdate(@FormDataParam(value = "batch") List<Student> batch) {

		try {
			if (batch.size() == 0) {
				throw new IllegalArgumentException("Batch update can not be empty");

			}

			studentTemplate = (StudentJDBCTemplate) request.getServletContext().getAttribute("studentJDBCtemplate");
			List<Student> students = batch;
			Assert.notNull(students, "Batch update can not be null");
			for (Student s : students) {
				try {
					StudentValidator.validateStudent(String.valueOf(s.getId()), s.getName(), String.valueOf(s.getAge()),
							s.getEmail());
				} catch (StudentException e) {
					logger.error(e.getMessage(), e);
				}
			}

			studentTemplate.executeBatchUpdate(students);

		} catch (IllegalArgumentException e) {

			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return Response.ok().build();
	}

	/**
	 * Receives InputStream byte stream and writes as characters into StringBuilder
	 * and returns as byte array
	 * 
	 * @param inputStream
	 * @return array
	 */
	public static byte[] imageBytes(InputStream inputStream) {
		StringBuffer img = new StringBuffer();

		try (BufferedInputStream bos = new BufferedInputStream(inputStream)) {

			int content = bos.read();
			while (content != -1) {
				img.append((char) content);
				content = bos.read();
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		byte[] array = img.toString().getBytes();
		if (array.length < 1) {
			array = null;
		}
		return array;
	}

}
