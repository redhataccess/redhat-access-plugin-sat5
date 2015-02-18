%define version 0.1.0

Name:	sat5-telemetry
Version:	%{version}
Release:	1%{?dist}
Summary:	Satellite 5 Telemetry
Group:	Applications/System	
License:	MIT
Source0: sat5-telemetry-0.1.0.tar.gz 
BuildArch: noarch
#BuildRequires:	
#Requires:	

%description
Telemetry


%prep
%setup -q


%build
echo OK


%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/etc/httpd/conf.d
cp rh-telemetry-proxy-sat5.6.conf $RPM_BUILD_ROOT/etc/httpd/conf.d/rh-telemetry-proxy-sat5.6.conf
mkdir -p $RPM_BUILD_ROOT/var/www/html/javascript
cp scripts.js $RPM_BUILD_ROOT/var/www/html/javascript/rhn-third-party.js
mkdir -p $RPM_BUILD_ROOT/usr/share/tomcat6/webapps
cp insights.war $RPM_BUILD_ROOT/usr/share/tomcat6/webapps


%files
%config %attr(0644,root,root) /etc/httpd/conf.d/rh-telemetry-proxy-sat5.6.conf
%config %attr(0644,root,root) /var/www/html/javascript/rhn-third-party.js
%config %attr(0644,root,root) /usr/share/tomcat6/webapps/insights.war


%changelog
* Thu Feb 12 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Version 1

