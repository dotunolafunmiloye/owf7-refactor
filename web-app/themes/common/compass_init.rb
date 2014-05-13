# include the utils rb file which has extra functionality for the owf theme

# Compass Configuration
Compass.configuration.relative_assets = true
Compass.configuration.css_dir = "css"
Compass.configuration.css_path = File.join(Compass.configuration.project_path,Compass.configuration.css_dir)
Compass.configuration.output_style = :expanded  # will be compressed later, anyway

FileUtils.mkdir_p(Compass.configuration.css_path)

$owf_common_theme_path = Pathname.new(File.dirname(__FILE__)).relative_path_from(Pathname.new(Compass.configuration.project_path))

$theme_name="default"

Compass::Frameworks.register_directory "../../js-lib/ext-4.0.7/resources/themes"

#Overrides some things from EXT to make it work with this theme
require File.join($owf_common_theme_path, 'lib', 'owf_utils.rb')

# register owf-common as a compass framework
Compass::Frameworks.register 'owf-common', $owf_common_theme_path




