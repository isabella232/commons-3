package io.onedev.commons.launcher.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.onedev.commons.launcher.loader.AppLoader;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AppLoader.class)
public abstract class AppLoaderMocker {

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AppLoader.class);
        
        setup();
    }
    
    @After
    public void after() {
        teardown();
    }
    
    protected abstract void setup();
    
    protected abstract void teardown();

}
