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
Setup a forward tunneling proxy to the Red Hat Customer Portal,
GUI extensions, and a config channel in a Satellite 5 server.


%prep
%setup -q


%build
echo OK


%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/etc/httpd/conf.d
cp rh-telemetry-proxy-sat5.6.conf $RPM_BUILD_ROOT/etc/httpd/conf.d/rh-telemetry-proxy-sat5.6.conf


%files
%config %attr(0644,root,root) /etc/httpd/conf.d/rh-telemetry-proxy-sat5.6.conf


%changelog
* Tue Jan 27 2015 Chris Kyrouac <ckyrouac@redhat.com>
- First version with httpd proxy.conf

