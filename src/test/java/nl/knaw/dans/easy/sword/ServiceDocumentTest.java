package nl.knaw.dans.easy.sword;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.easy.data.federation.FederativeUserRepo;
import nl.knaw.dans.easy.data.userrepo.EasyUserRepo;
import nl.knaw.dans.easy.domain.federation.FederativeUserIdMap;
import nl.knaw.dans.easy.domain.model.user.EasyUser;
import nl.knaw.dans.easy.domain.user.EasyUserImpl;
import nl.knaw.dans.easy.servicelayer.services.FederativeUserService;
import nl.knaw.dans.easy.servicelayer.services.Services;
import nl.knaw.dans.easy.servicelayer.services.UserService;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.purl.sword.base.SWORDErrorException;
import org.purl.sword.base.ServiceDocumentRequest;


import org.easymock.EasyMock;

public class ServiceDocumentTest extends EasySwordServerTester
{
    @BeforeClass
    public static void setupMocking() throws Exception {
        MockUtil.mockAll();
    }

    @Test
    public void serviceDocumentWithUser() throws Exception
    {
        final ServiceDocumentRequest request = new ServiceDocumentRequest();
        request.setUsername(MockUtil.VALID_USER_ID);
        request.setPassword(MockUtil.PASSWORD);
        request.setLocation(LOCATION);
        EasySwordServer.setPolicy("No guarantee of service, or that deposits will be retained for any length of time.");
        EasySwordServer.setTreatment("This is a test server");
        assertAsExpected(easySwordServer.doServiceDocument(request).toString(), "serviceDocumentWithUser.xml");
    }

    @Test(expected=SWORDErrorException.class)
    public void serviceDocumentWrongUser() throws Exception
    {
        final ServiceDocumentRequest request = new ServiceDocumentRequest();
        request.setUsername(MockUtil.INVALID_USER_ID);
        request.setPassword(MockUtil.PASSWORD);
        request.setLocation(LOCATION);
        assertAsExpected(easySwordServer.doServiceDocument(request).toString(), "serviceDocumentWrongUser.xml");
    }

    @Test//(expected=SWORDErrorException.class)
    public void serviceDocumentWithoutUser() throws Exception
    {
        final ServiceDocumentRequest request = new ServiceDocumentRequest();
        request.setLocation(LOCATION);
        assertAsExpected(easySwordServer.doServiceDocument(request).toString(), "serviceDocumentWithoutUser.xml");
    }
    

    // TODO maybe put this test elsewhere
    @Test
    public void calculateHashTest() throws Exception
    {
        String hash = easySwordServer.calculateHash("richardzijdeman@SURFguest.nl", "d49bcb3d-ffb6-4748-aef4-8ca6319f3afb");
        System.out.println("Hash=" + hash);
        // compare with known value
        assertEquals(hash, "f33bbf238a3157b0db8ab45088cc77d1d10bb640");
        // TODO more tests
    }
    
    // TODO maybe put this test elsewhere
    @Test
    public void extractUserIdTest() throws Exception
    {

        String id = easySwordServer.extractUserIdFromToken("richardzijdeman@SURFguest.nl"+"f33bbf238a3157b0db8ab45088cc77d1d10bb640");
        System.out.println("Id=" + id);
        assertEquals(id, "richardzijdeman@SURFguest.nl");
    }
    
    @Test
    public void extractUserIdWithWrongTokenTest() throws Exception
    {
        String id = null;
        
        id = easySwordServer.extractUserIdFromToken("");
        assertNull(id);
        
        id = easySwordServer.extractUserIdFromToken("rrrrrrrrrrrrrrrrrrrrrrrrrrrrf33bbf238a3157b0db8ab45088cc77d1d10bb640");
        assertNull(id);
        
        id = easySwordServer.extractUserIdFromToken("richardzijdeman@SURFguest.nlffffffffffffffffffffffffffffffffffffffff");
        assertNull(id);

        id = easySwordServer.extractUserIdFromToken("#richardzijdeman@SURFguest.nlf33bbf238a3157b0db8ab45088cc77d1d10bb640");
        assertNull(id);
    }
    
    
    @Test
    public void serviceDocumentWithFederativeUser() throws Exception
    {
        final FederativeUserRepo federativeUserRepo = EasyMock.createMock(FederativeUserRepo.class);
        final FederativeUserService federativeUserService = EasyMock.createMock(FederativeUserService.class);

        new Data().setFederativeUserRepo(federativeUserRepo);
        new Services().setFederativeUserService(federativeUserService);
        
        EasyUserRepo userRepo = EasyMock.createMock(EasyUserRepo.class);
        new Data().setUserRepo(userRepo);
        EasyUser easyUser = new EasyUserImpl();
        EasyMock.expect(userRepo.findById("richardzijdeman")).andReturn(easyUser);

        FederativeUserIdMap idMap = new FederativeUserIdMap("richardzijdeman@SURFguest.nl", "richardzijdeman");
        EasyMock.expect(federativeUserRepo.findById("richardzijdeman@SURFguest.nl")).andReturn(idMap);

        EasyMock.replay(userRepo);
        EasyMock.replay(federativeUserRepo);
        EasyMock.replay(federativeUserService);

        // Done mocking, so do the test
        final ServiceDocumentRequest request = new ServiceDocumentRequest();
        request.setUsername("nl.knaw.dans.easy.federatedUser");
        request.setPassword("richardzijdeman@SURFguest.nlf33bbf238a3157b0db8ab45088cc77d1d10bb640");
        request.setLocation(LOCATION);
        EasySwordServer.setPolicy("No guarantee of service, or that deposits will be retained for any length of time.");
        EasySwordServer.setTreatment("This is a test server");
        assertAsExpected(easySwordServer.doServiceDocument(request).toString(), "serviceDocumentWithFederativeUser.xml");
        
    }
    
}
