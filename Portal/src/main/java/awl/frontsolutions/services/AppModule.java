package awl.frontsolutions.services;

import java.io.IOException;

import net.atos.kawwaportal.components.KawwaConstants;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.MarkupRendererFilter;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.ResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.got5.tapestry5.jquery.JQuerySymbolConstants;
import org.slf4j.Logger;

import awl.frontsolutions.pages.GAnalyticsScriptsInjector;
import awl.frontsolutions.services.atos.AtosModule;
import awl.frontsolutions.services.impl.AtosServiceDefaultImpl;
import awl.frontsolutions.services.impl.AuthentificationDefaultImpl;
import awl.frontsolutions.services.impl.ComponentUtilsImpl;
import awl.frontsolutions.services.impl.ComponentZipFillerImpl;
import awl.frontsolutions.services.impl.DASSecurityManagerImpl;
import awl.frontsolutions.services.impl.DefaultMailServiceImpl;
import awl.frontsolutions.services.impl.FileSystemIndexerImpl;
import awl.frontsolutions.services.impl.TopComponentImpl;
import awl.frontsolutions.services.stack.Theme0Stack;
import awl.frontsolutions.services.stack.Theme1Stack;
import awl.frontsolutions.services.stack.Theme2Stack;
import awl.frontsolutions.services.stack.ThemeStack;
import awl.frontsolutions.services.stack.ThemeSwitcherStack;

@SubModule(AtosModule.class)
public class AppModule {
	
	@Startup
	public static void parseComponents(FileSystemIndexer indexer, TopComponent topComponent){
		topComponent.setTopComponent();
	}
	
	
	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void configure(MappedConfiguration<String, String> configuration) {

		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en, fr");

		configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
		
		configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "false");

		configuration.add(SymbolConstants.APPLICATION_VERSION, "0.0.2-SNAPSHOT");
		
		configuration.add(JQuerySymbolConstants.JQUERY_UI_DEFAULT_THEME,"classpath:org/got5/tapestry5/jquery/themes/jquery-ui.css");
		
		configuration.add(KawwaConstants.KAWWA_INCLUDE_STACK, "false");
		
		configuration.add("IncludeStack", "false");
		
		configuration.add("enableAnalytics", "false");
		
		configuration.add("enableDocumentationBlock", "false");
	}

	public void contributeMetaDataLocator(MappedConfiguration<String,String> configuration)
	{
	    configuration.add(MetaDataConstants.SECURE_PAGE, "true");
	}
	
	@Contribute(JavaScriptStackSource.class)
	public static void addKawwa2Themes(
			MappedConfiguration<String, JavaScriptStack> configuration) {
		configuration.addInstance("themestack", ThemeSwitcherStack.class);
		configuration.addInstance(ThemeStack.DEFAULT_THEME, Theme0Stack.class);
		configuration.addInstance(ThemeStack.PREFIX+"k-theme1", Theme1Stack.class);
		configuration.addInstance(ThemeStack.PREFIX+"k-theme2", Theme2Stack.class);
	}


	public static void bind(ServiceBinder binder) {

		binder.bind(FileSystemIndexer.class, FileSystemIndexerImpl.class);
		binder.bind(ComponentZipFiller.class, ComponentZipFillerImpl.class);
		binder.bind(ComponentUtils.class, ComponentUtilsImpl.class);		
		binder.bind(DASSecurityManager.class, DASSecurityManagerImpl.class);
		binder.bind(TopComponent.class, TopComponentImpl.class);
		binder.bind(AtosService.class, AtosServiceDefaultImpl.class);
		binder.bind(Authentification.class, AuthentificationDefaultImpl.class);
		binder.bind(MailService.class, DefaultMailServiceImpl.class);
	}	
	
	
	@Contribute(MarkupRenderer.class) 
    public static void addMetaRenderFilter( 
            OrderedConfiguration<MarkupRendererFilter> configuration, 
            final Environment environment, @Symbol("enableAnalytics") final boolean enableAnalytics
    ) { 
        MarkupRendererFilter rendererFilter = new MarkupRendererFilter() { 
            public void renderMarkup(MarkupWriter writer,MarkupRenderer renderer) { 
               
                renderer.renderMarkup(writer); 
                Element el = writer.getDocument().getRootElement().find("head");
                if(el!=null){
                	el.elementAt(0, "meta", "content", "IE=edge", "http-equiv" , "X-UA-Compatible");
                	el.elementAt(1, "meta", "content", "text/html; charset=utf-8", "http-equiv" , "Content-Type");
                	el.elementAt(2, "meta", "name", "viewport", "content" , "width=device-width, initial-scale=1.0");
                }
              } 
            
        }; 
        configuration.add("meta", rendererFilter, "before:*"); 
        
        if (enableAnalytics) {
			configuration.addInstance("GAnalyticsScript", GAnalyticsScriptsInjector.class, "after:DocumentLinker");
		}

    }
	
	
    public RequestExceptionHandler buildAppRequestExceptionHandler(
            final Logger logger,
            final ResponseRenderer renderer,
            final ComponentSource componentSource)
    {
        return new RequestExceptionHandler()
        {
            public void handleRequestException(Throwable exception) throws IOException
            {
                logger.error("Unexpected runtime exception: " + exception.getMessage(), exception);

                renderer.renderPageMarkupResponse("Index");
            }
        };
    }

    public void contributeServiceOverride(
            MappedConfiguration<Class, Object> configuration,

            @Local
            RequestExceptionHandler handler)
    {
        configuration.add(RequestExceptionHandler.class, handler);
    }
}
