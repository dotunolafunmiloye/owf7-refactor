module OWF
  module SassExtensions
    module Functions
      module Utils
        
        # Override the "theme_image" function provided by ExtJS
        # This accounts for the actual configuration information, rather than assumed
        # directory structures
        def theme_image(theme, path, without_url = false, relative = false)
          path = path.value
          theme = theme.value + '.theme'
          without_url = (without_url.class == FalseClass) ? without_url : without_url.value
          
          # Should be css_dir, but the css_dir may not exist at this point
          # making the "relative_path_from" call fail on unix systems
          relative_path = Compass.configuration.css_path
          
          owf_common_theme_images = File.join(Compass.configuration.project_path,$owf_common_theme_path,"images")
          
				  if relative
            if relative.class == Sass::Script::String
              relative_path = relative.value
              relative = true
            end
          else
            relative = false
          end

          if File.exists?(File.join(Compass.configuration.images_path,path))
          	image_path = Pathname.new(Compass.configuration.images_path).relative_path_from(Pathname.new(relative_path))
          else
            image_path = Pathname.new(owf_common_theme_images).relative_path_from(Pathname.new(relative_path))
          end

					image_path=File.join(image_path,path)
          
          if !without_url
            url = "url('#{image_path}')"
          else
            url = "#{image_path}"
          end
                    
          Sass::Script::String.new(url)
        end


				# Overriding the ExtJS theme_image_exists
				# All image paths should be relative to the directory that the CSS for this theme lives in
				# The ExtJS version makes assumptions about where that directory is
        def theme_image_exists(path)
          result = false

          where_to_look = File.join(Compass.configuration.css_path,path.value)


          if where_to_look && FileTest.exists?("#{where_to_look}")
            result = true
          else
         	 puts <<-DEBUGGING
Looking for #{path.value}
	where_to_look= #{where_to_look}
	Compass.configuration.css_path = #{Compass.configuration.css_path}
	Compass.configuration.images_path = #{Compass.configuration.images_path}
	PWD is #{Dir.pwd}
DEBUGGING
					end
          	

          return Sass::Script::Bool.new(result)
        end

        #Takes either a list containing a color followed by a color stop percentage, or 
        #just a color.  Returns the color
        def strip_stop(color_w_stop)
            (color_w_stop.class == Sass::Script::Color) ? color_w_stop : color_w_stop.to_a[0]
        end

      end # module Utils
    end # module Functions
  end # module SassExtensions
end # module OWF

module Sass::Script::Functions
  include OWF::SassExtensions::Functions::Utils
end
