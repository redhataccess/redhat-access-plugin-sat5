%define version 0.1.0

Name:	sat5-telemetry
Version:	%{version}
Release:	1%{?dist}
Summary:	Satellite 5 Telemetry
Group:	Applications/System	
License:	MIT
Source0: sat5-insights.tar.gz 
BuildArch: noarch
#BuildRequires:	
#Requires:	

%description
Red Hat Access Insights 


%prep
%setup -q


%build
echo OK


%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/etc/httpd/conf.d
cp rh-insights-sat5.conf $RPM_BUILD_ROOT/etc/httpd/conf.d/rh-insights-sat5.conf
mkdir -p $RPM_BUILD_ROOT/var/www/html/javascript
cp insights.js $RPM_BUILD_ROOT/var/www/html/javascript/insights.js
mkdir -p $RPM_BUILD_ROOT/usr/share/tomcat6/webapps
cp redhat_access.war $RPM_BUILD_ROOT/usr/share/tomcat6/webapps


%files
%config %attr(0644,root,root) /etc/httpd/conf.d/rh-insights-sat5.conf
%config %attr(0644,root,root) /var/www/html/javascript/insights.js
%config %attr(0644,root,root) /usr/share/tomcat6/webapps/redhat_access.war


%changelog
* Thu Feb 12 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Version 1

